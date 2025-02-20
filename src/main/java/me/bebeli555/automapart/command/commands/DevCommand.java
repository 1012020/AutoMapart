package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.command.CommandParameter;
import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.utils.ClientUtils;
import me.bebeli555.automapart.utils.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Formatting;

import java.util.stream.Collectors;

public class DevCommand extends Command {
    public static boolean printNextSettings;

    public DevCommand() {
        super("dev", "Some developer commands. Don't touch!",
                new CommandParameter(0, "scale", "value"),
                new CommandParameter(0, "printsettings"),
                new CommandParameter(0, "window", "name", () -> {
                    active.completions = TitledWindow.list.stream().map(w -> w.title).collect(Collectors.toList());
                }),
                new CommandParameter(0, "attributes")
        );
        this.newLine = true;
    }

    @Override
    public void onCommand(String[] parameter) {
        switch (parameter[0]) {
            case "scale" -> {
                mc.options.getGuiScale().setValue(Integer.parseInt(parameter[1]));
                ClientUtils.sendMessage("Set gui scale to " + parameter[1]);
            }
            case "printsettings" -> {
                printNextSettings = true;
                ClientUtils.sendMessage("Next setting save will be printed to console");
            }
            case "window" -> {
                TitledWindow found = null;
                for (TitledWindow window : TitledWindow.list) {
                    if (window.title.equals(parameter[1].replace("_", " "))) {
                        found = window;
                        break;
                    }
                }
                if (found == null) {
                    ClientUtils.sendMessage("Couldn't find window with that name", Formatting.RED);
                } else {
                    ClientUtils.sendMessage(found.title + " window is opening in 1.5 seconds, open ur GUI!");

                    final TitledWindow finalFound = found;
                    new Thread(() -> {
                        sleep(1500);
                        mc.execute(() -> finalFound.enable(0, 0));
                    }).start();
                }
            }
            case "test" -> {
                LivingEntity closest = null;
                for (Entity entity : EntityUtils.getAllWithoutClientPlayer()) {
                    if (entity instanceof LivingEntity livingEntity) {
                        if (closest == null || entity.distanceTo(mc.player) < closest.distanceTo(mc.player)) {
                            closest = livingEntity;
                        }
                    }
                }
            }
        }
    }
}
