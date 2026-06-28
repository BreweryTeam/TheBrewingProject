package dev.jsinco.brewery.bukkit.database;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.database.barrel.BarrelSession;
import dev.jsinco.brewery.bukkit.database.barrel.SqLiteBarrelSession;
import dev.jsinco.brewery.bukkit.database.cauldron.CauldronSession;
import dev.jsinco.brewery.bukkit.database.cauldron.SqLiteCauldronSession;
import dev.jsinco.brewery.bukkit.database.distillery.DistillerySession;
import dev.jsinco.brewery.bukkit.database.distillery.SqLiteDistillerySession;
import dev.jsinco.brewery.bukkit.database.drunk.SqLiteDrunkStateSession;
import dev.jsinco.brewery.bukkit.database.misc.MiscSession;
import dev.jsinco.brewery.bukkit.database.misc.SqLiteMiscSession;
import dev.jsinco.brewery.database.PersistenceException;
import dev.jsinco.brewery.database.PersistenceHandler;
import dev.jsinco.brewery.database.SessionType;
import dev.jsinco.brewery.database.session.DrunkenStateSession;
import dev.jsinco.brewery.database.sql.SqlDatabase;

import java.util.concurrent.Executor;

public final class SessionTypes {

    public static final SessionType<BarrelSession> BARREL_SESSION_TYPE = SessionTypes::newBarrelSession;
    public static final SessionType<DistillerySession> DISTILLERY_SESSION_TYPE = SessionTypes::newDistillerySession;
    public static final SessionType<CauldronSession> CAULDRON_SESSION_TYPE = SessionTypes::newCauldronSession;
    public static final SessionType<MiscSession> MISC_SESSION_TYPE = SessionTypes::newMiscSession;
    public static final SessionType<DrunkenStateSession> DRUNK_STATE_SESSION_TYPE = SessionTypes::newDrunkStateSession;


    private static BarrelSession newBarrelSession(Executor executor, PersistenceHandler handler) throws PersistenceException {
        if (!(handler instanceof SqlDatabase database)) {
            throw new IllegalStateException("Unknown persistence handler: " + handler.getClass().getName());
        }
        return switch (handler.driver()) {
            case SQLITE ->
                    new SqLiteBarrelSession(executor, database::getConnection, TheBrewingProject.getInstance().getResolvedIngredientManager());
            default -> throw new IllegalStateException("Unknown driver: " + handler.driver());
        };
    }

    private static DistillerySession newDistillerySession(Executor executor, PersistenceHandler handler) throws PersistenceException {
        if (!(handler instanceof SqlDatabase database)) {
            throw new IllegalStateException("Unknown persistence handler: " + handler.getClass().getName());
        }
        return switch (handler.driver()) {
            case SQLITE ->
                    new SqLiteDistillerySession(executor, database::getConnection, TheBrewingProject.getInstance().getResolvedIngredientManager());
            default -> throw new IllegalStateException("Unknown driver: " + handler.driver());
        };
    }

    private static CauldronSession newCauldronSession(Executor executor, PersistenceHandler handler) throws PersistenceException {
        if (!(handler instanceof SqlDatabase database)) {
            throw new IllegalStateException("Unknown persistence handler: " + handler.getClass().getName());
        }
        return switch (handler.driver()) {
            case SQLITE ->
                    new SqLiteCauldronSession(executor, database::getConnection, TheBrewingProject.getInstance().getResolvedIngredientManager());
            default -> throw new IllegalStateException("Unknown driver: " + handler.driver());
        };
    }

    private static MiscSession newMiscSession(Executor executor, PersistenceHandler handler) throws PersistenceException {
        if (!(handler instanceof SqlDatabase database)) {
            throw new IllegalStateException("Unknown persistence handler: " + handler.getClass().getName());
        }
        return switch (handler.driver()) {
            case SQLITE -> new SqLiteMiscSession(executor, database::getConnection);
            default -> throw new IllegalStateException("Unknown driver: " + handler.driver());
        };
    }

    private static DrunkenStateSession newDrunkStateSession(Executor executor, PersistenceHandler handler) throws PersistenceException {
        if (!(handler instanceof SqlDatabase database)) {
            throw new IllegalStateException("Unknown persistence handler: " + handler.getClass().getName());
        }
        return switch (handler.driver()) {
            case SQLITE -> new SqLiteDrunkStateSession(executor, database::getConnection);
            default -> throw new IllegalStateException("Unknown driver: " + handler.driver());
        };
    }

    private SessionTypes() {
        throw new UnsupportedOperationException("Utility class");
    }

}
