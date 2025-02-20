package me.bebeli555.automapart.gui.windows.components;

import me.bebeli555.automapart.events.game.KeyInputEvent;

public interface WindowComponent {
    void onComponentClick(int mouseX, int mouseY, int button);
    void onComponentChar(char chr);
    void onComponentKey(KeyInputEvent event);
}
