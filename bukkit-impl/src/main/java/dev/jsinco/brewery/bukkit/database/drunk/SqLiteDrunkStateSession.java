package dev.jsinco.brewery.bukkit.database.drunk;

import dev.jsinco.brewery.api.effect.DrunkState;
import dev.jsinco.brewery.api.effect.modifier.DrunkenModifier;
import dev.jsinco.brewery.configuration.DrunkenModifierSection;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.PersistenceSupplier;
import dev.jsinco.brewery.database.session.DrunkenStateSession;
import dev.jsinco.brewery.database.sql.SqlStatements;
import dev.jsinco.brewery.effect.DrunkStateImpl;
import dev.jsinco.brewery.util.DecoderEncoder;
import dev.jsinco.brewery.util.FutureUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public record SqLiteDrunkStateSession(Executor executor,
                                      PersistenceSupplier<Connection> connectionSupplier) implements DrunkenStateSession {

    private static final SqlStatements MODIFIER_STATEMENTS = new SqlStatements("/database/generic/modifiers");

    private static final SqlStatements STATE_STATEMENTS = new SqlStatements("/database/generic/drunk_states");

    @Override
    public CompletableFuture<Void> insertModifier(DrunkenModifier drunkenModifier, double value, UUID playerUuid) {
        return execute(() -> {
            try (Connection connection = connectionSupplier.getUnchecked(); PreparedStatement preparedStatement = connection.prepareStatement(MODIFIER_STATEMENTS.get(SqlStatements.Type.INSERT))) {
                preparedStatement.setBytes(1, DecoderEncoder.asBytes(playerUuid));
                preparedStatement.setString(2, drunkenModifier.name());
                preparedStatement.setDouble(3, value);
                preparedStatement.execute();
            } catch (SQLException e) {
                throw new PersistenceException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> removeModifier(DrunkenModifier modifier, UUID playerUuid) {
        return execute(() -> {
            try (Connection connection = connectionSupplier.getUnchecked(); PreparedStatement preparedStatement = connection.prepareStatement(MODIFIER_STATEMENTS.get(SqlStatements.Type.DELETE))) {
                preparedStatement.setBytes(1, DecoderEncoder.asBytes(playerUuid));
                preparedStatement.setString(2, modifier.name());
                preparedStatement.execute();
            } catch (SQLException e) {
                throw new PersistenceException(e);
            }
        });
    }

    @Override
    public CompletableFuture<List<ModifierLookupResult>> fetchDrunkenModifiers(UUID playerUuid) {
        return fetch(() -> {
            try (Connection connection = connectionSupplier.getUnchecked(); PreparedStatement preparedStatement = connection.prepareStatement(MODIFIER_STATEMENTS.get(SqlStatements.Type.FIND))) {
                preparedStatement.setBytes(1, DecoderEncoder.asBytes(playerUuid));
                ResultSet query = preparedStatement.executeQuery();
                List<ModifierLookupResult> modifiers = new ArrayList<>();
                while (query.next()) {
                    String modifierName = query.getString("modifier_name");
                    double value = query.getDouble("value");
                    DrunkenModifierSection.modifiers().drunkenModifiers().stream().filter(modifier -> modifier.name().equals(modifierName))
                            .findAny()
                            .map(modifier -> new ModifierLookupResult(modifier, value))
                            .ifPresent(modifiers::add);
                }
                return modifiers;
            } catch (SQLException e) {
                throw new PersistenceException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> insertState(DrunkState state, UUID playerUuid) {
        return execute(() -> {
            try (Connection connection = connectionSupplier.getUnchecked(); PreparedStatement preparedStatement = connection.prepareStatement(STATE_STATEMENTS.get(SqlStatements.Type.INSERT))) {
                preparedStatement.setBytes(1, DecoderEncoder.asBytes(playerUuid));
                preparedStatement.setLong(2, state.kickedTimestamp());
                preparedStatement.setLong(3, state.timestamp());
                preparedStatement.execute();
            } catch (SQLException e) {
                throw new PersistenceException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> removeState(UUID playerUuid) {
        return CompletableFuture.allOf(execute(() -> {
            try (Connection connection = connectionSupplier.getUnchecked(); PreparedStatement preparedStatement = connection.prepareStatement(STATE_STATEMENTS.get(SqlStatements.Type.DELETE))) {
                preparedStatement.setBytes(1, DecoderEncoder.asBytes(playerUuid));
                preparedStatement.execute();
            } catch (SQLException e) {
                throw new PersistenceException(e);
            }
        }), execute(() -> {
            try (Connection connection = connectionSupplier.getUnchecked(); PreparedStatement preparedStatement = connection.prepareStatement(MODIFIER_STATEMENTS.get("clear_player"))) {
                preparedStatement.setBytes(1, DecoderEncoder.asBytes(playerUuid));
                preparedStatement.execute();
            } catch (SQLException e) {
                throw new PersistenceException(e);
            }
        }));
    }

    @Override
    public CompletableFuture<Void> updateState(DrunkState state, UUID playerUuid) {
        return execute(() -> {
            try (Connection connection = connectionSupplier.getUnchecked(); PreparedStatement preparedStatement = connection.prepareStatement(STATE_STATEMENTS.get(SqlStatements.Type.UPDATE))) {
                preparedStatement.setLong(1, state.kickedTimestamp());
                preparedStatement.setLong(2, state.timestamp());
                preparedStatement.setBytes(3, DecoderEncoder.asBytes(playerUuid));
                preparedStatement.execute();
            } catch (SQLException e) {
                throw new PersistenceException(e);
            }
        });
    }

    @Override
    public CompletableFuture<List<StateLookupResult>> retrieveAllStates() {
        CompletableFuture<List<StateLookupResult>> completed = new CompletableFuture<>();
        fetch(() -> {
            List<StateLookupResult> drunks = new ArrayList<>();
            try (Connection connection = connectionSupplier.getUnchecked(); PreparedStatement preparedStatement = connection.prepareStatement(STATE_STATEMENTS.get(SqlStatements.Type.SELECT_ALL))) {
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    drunks.add(new StateLookupResult(
                            new DrunkStateImpl(resultSet.getLong("time_stamp"),
                                    resultSet.getLong("kicked_timestamp"),
                                    Map.of()
                            ),
                            DecoderEncoder.asUuid(resultSet.getBytes("player_uuid"))
                    ));
                }
            } catch (SQLException e) {
                throw new PersistenceException(e);
            }
            return drunks;
        }).thenAcceptAsync(states -> {
            List<CompletableFuture<StateLookupResult>> outputFutures = new ArrayList<>();
            for (StateLookupResult state : states) {
                outputFutures.add(fetchDrunkenModifiers(state.playerUuid())
                        .thenApply(modifiers -> new StateLookupResult(
                                state.state().withModifiers(modifiers.stream()
                                        .collect(Collectors.toMap(ModifierLookupResult::modifier, ModifierLookupResult::value))
                                ),
                                state.playerUuid()
                        )));
            }
            FutureUtil.mergeFutures(outputFutures)
                    .thenAccept(completed::complete);
        }, executor);
        return completed;
    }
}
