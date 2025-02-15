package me.bebeli555.automapart.command.commands;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.command.Command;
import com.google.common.eventbus.Subscribe;
import me.bebeli555.automapart.events.entity.EntityRemovedEvent;
import me.bebeli555.automapart.hud.components.InfoClusterComponent;
import me.bebeli555.automapart.utils.ClientUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class LastDeathCommand extends Command {
    public BlockPos pos;
    public long ago;

    public LastDeathCommand() {
        super("lastdeath", "Shows your last death location and ago time");
        Mod.EVENT_BUS.register(this);
    }

    @Override
    public void onCommand(String[] parameter) {
        if (pos == null) {
            ClientUtils.sendMessage("You haven't died yet :D");
        } else {
            ClientUtils.sendMessage("Last death: " + "X:" + pos.getX() + ", " + "Y:" + pos.getY() + ", " + "Z:" + pos.getZ() + " -" + InfoClusterComponent.getTimeAgoString(ago, true) + " ago");
        }
    }

    @Subscribe
    public void entityRemoved(EntityRemovedEvent e) {
        if (e.removalReason == Entity.RemovalReason.KILLED && e.entity.equals(mc.player)) {
            pos = mc.player.getBlockPos();
            ago = System.currentTimeMillis();
        }
    }
}
