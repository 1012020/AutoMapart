package me.bebeli555.automapart.events.game;

import net.minecraft.client.gui.screen.Screen;

public class SetScreenEvent {
    public Screen screen, prevScreen;

    public SetScreenEvent(Screen screen, Screen prevScreen) {
        this.screen = screen;
        this.prevScreen = prevScreen;
    }
}
