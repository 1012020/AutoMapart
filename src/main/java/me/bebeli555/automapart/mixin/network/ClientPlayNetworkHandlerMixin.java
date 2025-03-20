package me.bebeli555.automapart.mixin.network;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.game.ClientChatEvent;
import me.bebeli555.automapart.events.game.SendChatMessageEvent;
import me.bebeli555.automapart.events.player.PlayerListEntryJoinEvent;
import me.bebeli555.automapart.utils.MixinUtils;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin<E> {
    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String content, CallbackInfo ci) {
        ClientChatEvent event = new ClientChatEvent(content);
        Mod.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerListAction", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
    private void addPlayerListEntry(PlayerListS2CPacket.Action action, PlayerListS2CPacket.Entry receivedEntry, PlayerListEntry currentEntry, CallbackInfo ci) {
        MixinUtils.basicEvent(new PlayerListEntryJoinEvent(currentEntry), ci);
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void sendChatMessage(String content, CallbackInfo ci) {
        MixinUtils.basicEvent(new SendChatMessageEvent(content), ci);
    }
}
