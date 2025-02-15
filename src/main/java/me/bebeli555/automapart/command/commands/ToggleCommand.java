package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.command.CommandParameter;
import me.bebeli555.automapart.utils.ClientUtils;
import net.minecraft.util.Formatting;

import java.util.stream.Collectors;

public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle", "Toggle a module on or off", new CommandParameter(0, "module", () -> {
            active.completions = Mod.modules.stream().map(m -> m.name).collect(Collectors.toList());
        }));
    }

    @Override
    public void onCommand(String[] parameter) {
        Mod mod = null;
        for (Mod module : Mod.modules) {
            if (module.name.equalsIgnoreCase(parameter[0])) {
                mod = module;
                break;
            }
        }

        if (mod == null) {
            ClientUtils.sendMessage("Cant find a module named " + parameter[0], Formatting.RED);
        } else {
            mod.toggle();
            ClientUtils.sendMessage("Toggled " + mod.name + " " + (mod.isOn() ? "ON" : "OFF"));
        }
    }
}
