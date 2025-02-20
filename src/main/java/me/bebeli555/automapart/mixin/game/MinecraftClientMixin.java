package me.bebeli555.automapart.mixin.game;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.entity.IsEntityGlowingEvent;
import me.bebeli555.automapart.events.game.ClientTickEvent;
import me.bebeli555.automapart.events.game.ClientTickPostEvent;
import me.bebeli555.automapart.events.game.GamePostInitEvent;
import me.bebeli555.automapart.events.game.SetScreenEvent;
import me.bebeli555.automapart.utils.MixinUtils;
import me.bebeli555.automapart.utils.Utils;
import me.bebeli555.automapart.utils.objects.MixinParameters;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ClientTickEvent event = new ClientTickEvent();
        Mod.EVENT_BUS.post(event);

        if (Utils.mc.player != null) {
            ClientTickEvent.Playing event1 = new ClientTickEvent.Playing();
            Mod.EVENT_BUS.post(event1);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickReturn(CallbackInfo ci) {
        ClientTickPostEvent event = new ClientTickPostEvent();
        Mod.EVENT_BUS.post(event);

        if (Utils.mc.player != null) {
            MixinParameters.timerPrevPos = MixinParameters.timerPos;
            MixinParameters.timerPos = Utils.mc.player.getPos();
        }
    }

    @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
    private void hasOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        IsEntityGlowingEvent event = new IsEntityGlowingEvent(entity);
        Mod.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            cir.setReturnValue(event.glowing);
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(RunArgs args, CallbackInfo ci) {
        MixinUtils.basicEvent(new GamePostInitEvent(), ci);
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void setScreen(Screen screen, CallbackInfo ci) {
        MixinUtils.basicEvent(new SetScreenEvent(screen, Utils.mc.currentScreen), ci);
    }

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    private void sendSwapPacket(CallbackInfo ci) {

    }

    @Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/Perspective;next()Lnet/minecraft/client/option/Perspective;"))
    private Perspective nextPerspective(Perspective perspective) {
        return perspective.next();
    }
}
