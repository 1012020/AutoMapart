package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.command.CommandParameter;
import net.minecraft.util.math.Vec3d;

public class ClipCommand extends Command {
    public ClipCommand() {
        super("clip", "Teleport to a pos AWAY from the player", new CommandParameter(0, "x"), new CommandParameter(1, "y"), new CommandParameter(2, "z"));
        this.newLine = true;
    }

    @Override
    public void onCommand(String[] parameter) {
        Vec3d vec = new Vec3d(getDouble(parameter[0]) + mc.player.getPos().x, getDouble(parameter[1]) + mc.player.getPos().y, getDouble(parameter[2]) + mc.player.getPos().z);
        mc.player.setPos(vec.x, vec.y, vec.z);
    }
}
