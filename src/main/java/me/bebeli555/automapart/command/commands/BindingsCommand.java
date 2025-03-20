package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.gui.GuiNode;
import me.bebeli555.automapart.utils.ClientUtils;
import me.bebeli555.automapart.utils.input.Keyboard;

public class BindingsCommand extends Command {
    public BindingsCommand() {
        super("bindings", "Shows all your active keybinds");
    }

    @Override
    public void onCommand(String[] parameter) {
        for (GuiNode node : GuiNode.all) {
            if (node.isKeybind) {
                if (node.stringValue.isEmpty()) {
                    continue;
                }

                if (node.parent == null) {
                    ClientUtils.sendMessage("GUI: " + Keyboard.getKeyName(Integer.parseInt(node.stringValue)));
                } else {
                    ClientUtils.sendMessage(node.parent.name + ": " + node.stringValue);
                }
            }
        }
    }
}
