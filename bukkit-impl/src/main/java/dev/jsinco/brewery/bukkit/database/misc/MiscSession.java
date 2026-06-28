package dev.jsinco.brewery.bukkit.database.misc;

import dev.jsinco.brewery.database.Session;

import java.util.concurrent.CompletableFuture;

public interface MiscSession extends Session<MiscSession> {

    CompletableFuture<Void> setTime(long time);

    CompletableFuture<Long> getTime();
}
