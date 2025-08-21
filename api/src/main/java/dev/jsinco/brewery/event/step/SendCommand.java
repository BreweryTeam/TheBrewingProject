package dev.jsinco.brewery.event.step;


import dev.jsinco.brewery.event.EventStepProperty;

public record SendCommand(String command, CommandSenderType senderType) implements EventStepProperty {

    public enum CommandSenderType {
        PLAYER,
        SERVER
    }
}