package me.bebeli555.automapart.events.game;

import me.bebeli555.automapart.events.Cancellable;

public class SendChatMessageEvent extends Cancellable {
    public String message;

    public SendChatMessageEvent(String message) {
        this.message = message;
    }
}
