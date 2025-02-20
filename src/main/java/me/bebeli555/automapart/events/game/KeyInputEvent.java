package me.bebeli555.automapart.events.game;

import me.bebeli555.automapart.events.Cancellable;

public class KeyInputEvent extends Cancellable {
    private final int key, scancode, action, modifiers;

    public KeyInputEvent(int key, int scancode, int action, int modifiers) {
        this.key = key;
        this.scancode = scancode;
        this.action = action;
        this.modifiers = modifiers;
    }

    public int getKey() {
        return key;
    }

    public int getScanCode() {
        return scancode;
    }

    public int getAction() {
        return action;
    }

    public int getModifiers() {
        return modifiers;
    }
}
