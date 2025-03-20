package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.command.CommandParameter;
import me.bebeli555.automapart.utils.Utils;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class SetBlockCommand extends Command {
    public SetBlockCommand() {
        super("setblock", "Spawns a block to a location clientside",
            new CommandParameter.CommandParameterGroup(
                    new CommandParameter(0, "name", () -> active.completions = Registries.BLOCK.stream().map(b -> b.getName().getString().replace(" ", "_")).collect(Collectors.toList())),
                    new CommandParameter(1, "x", () -> active.completions = new ArrayList<>(Collections.singleton("" + mc.player.getBlockPos().getX()))),
                    new CommandParameter(2, "y", () -> active.completions = new ArrayList<>(Collections.singleton("" + mc.player.getBlockPos().getY()))),
                    new CommandParameter(3, "z", () -> active.completions = new ArrayList<>(Collections.singleton("" + mc.player.getBlockPos().getZ())))
            )
        );
    }

    @Override
    public void onCommand(String[] parameter) {
        mc.player.getWorld().setBlockState(
                new BlockPos(Integer.parseInt(parameter[1]), Integer.parseInt(parameter[2]), Integer.parseInt(parameter[3])),
                Utils.getBlockFromName(parameter[0].replace("_", " ")).getDefaultState()
        );
    }
}
