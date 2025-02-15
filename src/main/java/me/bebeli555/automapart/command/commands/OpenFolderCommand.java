package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.settings.Settings;

import java.awt.*;
import java.io.File;

public class OpenFolderCommand extends Command {
    public OpenFolderCommand() {
        super("openfolder", "Opens the " + Mod.NAME + " directory");
    }

    @Override
    public void onCommand(String[] parameter) throws Exception {
        File file = new File (Settings.path);
        Desktop desktop = Desktop.getDesktop();
        desktop.open(file);
    }
}
