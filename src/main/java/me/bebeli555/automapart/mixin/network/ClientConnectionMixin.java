package me.bebeli555.automapart.mixin.network;

import io.netty.channel.ChannelHandlerContext;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.game.PacketClientEvent;
import me.bebeli555.automapart.events.game.PacketEvent;
import me.bebeli555.automapart.events.game.PacketPostEvent;
import me.bebeli555.automapart.events.game.PacketServerEvent;
import me.bebeli555.automapart.events.player.RotationPacketSend;
import me.bebeli555.automapart.settings.Settings;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("RETURN"), cancellable = true)
    private void onPostSend(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        PacketPostEvent event = new PacketPostEvent(packet);
        Mod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onChannelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        PacketEvent event = new PacketEvent(packet);
        Mod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }

        PacketServerEvent event1 = new PacketServerEvent(packet);
        Mod.EVENT_BUS.post(event1);
        if (event1.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("RETURN"), cancellable = true)
    private void onPostChannelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        PacketPostEvent event = new PacketPostEvent(packet);
        Mod.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "disconnect", at = @At("HEAD"))
    private void onDisconnect(Text disconnectReason, CallbackInfo ci) {
        //Also saves the settings when connection to server is closed.
        //Usually it saves them when u close the gui but if you just use keybinds and never open it, it wouldn't save so this will save it
        Settings.saveSettings();
    }
}
