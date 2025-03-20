package me.bebeli555.automapart.events.game;

import me.bebeli555.automapart.events.Cancellable;

public class ClientChatEvent extends Cancellable {
    private final String message;

    public ClientChatEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
