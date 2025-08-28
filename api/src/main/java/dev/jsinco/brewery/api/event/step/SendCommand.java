package dev.jsinco.brewery.api.event.step;


import dev.jsinco.brewery.api.event.EventStepProperty;

public record SendCommand(String command, CommandSenderType senderType) implements EventStepProperty {

    public enum CommandSenderType {
        PLAYER,
        SERVER
    }
}