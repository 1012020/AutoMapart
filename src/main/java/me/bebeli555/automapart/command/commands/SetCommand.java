package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.command.CommandParameter;
import me.bebeli555.automapart.gui.GuiNode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.Settings;
import me.bebeli555.automapart.utils.ClientUtils;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class SetCommand extends Command {
    public SetCommand() {
        super("set", "Set a value for a setting/module",
                new CommandParameter(0, "id", () -> {
                    active.completions = Setting.all.stream().map(s -> s.id.replace(" ", "_")).collect(Collectors.toList());
                }),
                new CommandParameter(1, "value", () ->  {
                    Setting setting = Setting.getSettingWithId(active.writtenCommand.split(" ")[1].replace("_", " "));
                    if (setting != null) {
                        String value = "" + setting.defaultValue;
                        active.completions = new ArrayList<>();

                        if (value.equals("false") || value.equals("true")) {
                            active.completions.add("true");
                            active.completions.add("false");
                        }
                    }
                })
        );
    }

    @Override
    public void onCommand(String[] parameter) {
        String id = parameter[0].replace("_", " ");
        String value = parameter[1];

        GuiNode guiNode = Settings.getGuiNodeFromId(id);
        if (guiNode == null) {
            ClientUtils.sendMessage("Cant find setting with id: " + id, Formatting.RED);
        } else {
            if (guiNode.isTypeable != Settings.isBoolean(value)) {
                if (!guiNode.isTypeable) {
                    guiNode.toggled = Boolean.parseBoolean(value);
                    guiNode.setSetting();
                } else {
                    try {
                        guiNode.stringValue = value;
                        guiNode.setSetting();
                    } catch (Exception ex) {
                        ClientUtils.sendMessage("Wrong input. This might be caused if u input a string value and the setting only accepts integer or double", Formatting.RED);
                    }
                }

                ClientUtils.sendMessage("Set " + id + " to " + value);
                if (Settings.isBoolean(value)) {
                    Mod mod = Mod.findMod(id);
                    if (mod != null) {
                        mod.toggle();
                    }
                }
            } else {
                if (guiNode.isTypeable) {
                    ClientUtils.sendMessage("This setting requires a boolean value", Formatting.RED);
                } else {
                    ClientUtils.sendMessage("This setting requires a string or integer value", Formatting.RED);
                }
            }
        }
    }
}
