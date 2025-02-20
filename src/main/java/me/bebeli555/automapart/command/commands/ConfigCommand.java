package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.command.CommandParameter;
import me.bebeli555.automapart.settings.Keybind;
import me.bebeli555.automapart.settings.Settings;
import me.bebeli555.automapart.utils.ClientUtils;
import net.minecraft.util.Formatting;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ConfigCommand extends Command {
    public ConfigCommand() {
        super("config", "Add and load different configs for the client",
            new CommandParameter.CommandParameterGroup(
                    new CommandParameter(0, "add/load/remove", "name", () -> {
                        if (!active.writtenCommand.split(" ")[1].equals("add")) {
                            active.completions = getConfigNames();
                        }
                    })
            ),
            new CommandParameter(0, "list")
        );
    }

    @Override
    public void onCommand(String[] parameter) throws Exception {
        //Create folder
        new File(Settings.path + "/configs/").mkdir();

        String action = parameter[0];
        String name = action.equals("list") ? null : parameter[1];
        boolean exists = new File(Settings.path + "/configs/" + name).exists();

        switch (action) {
            case "add", "save" -> {
                new File(Settings.path + "/configs/" + name).delete();
                Files.copy(Settings.settings.toPath(), new File(Settings.path + "/configs/" + name).toPath());
                ClientUtils.sendMessage("Added config: " + name);
            }
            case "remove" -> {
                if (exists) {
                    new File(Settings.path + "/configs/" + name).delete();
                    ClientUtils.sendMessage("Deleted config: " + name);
                } else {
                    ClientUtils.sendMessage("Config with name " + name + " doesn't exist", Formatting.RED);
                }
            }
            case "load" -> {
                if (exists) {
                    long ms = System.currentTimeMillis();
                    new File(Settings.path + "/configs/unsaved/").mkdir();
                    File unsaved = new File(Settings.path + "/configs/unsaved/" + ms);
                    Files.copy(Settings.settings.toPath(), unsaved.toPath());
                    for (File file : new File(Settings.path + "/configs/unsaved/").listFiles()) {
                        if (file.length() == unsaved.length() && !file.getName().equals(unsaved.getName())) {
                            unsaved.delete();
                            break;
                        }
                    }

                    Settings.settings.delete();
                    Files.copy(new File(Settings.path + "/configs/" + name).toPath(), Settings.settings.toPath());

                    Settings.loadSettings();
                    Keybind.setKeybinds();
                    ClientUtils.sendMessage("Loaded config: " + name);
                } else {
                    ClientUtils.sendMessage("No config with name " + name, Formatting.RED);
                }
            }
        }
    }

    public static List<String> getConfigNames() {
        List<String> list = new ArrayList<>();

        File file1 = new File(Settings.path + "/configs/");
        file1.mkdir();

        for (File file : file1.listFiles()) {
            if (file.isFile()) {
                list.add(file.getName());
            }
        }

        return list;
    }
}
