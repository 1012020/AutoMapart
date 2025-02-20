package me.bebeli555.automapart.mixin.world;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.render.SetWorldTimeEvent;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.Properties.class)
public class ClientWorldPropertiesMixin {
    @Inject(method = "setTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void injectSetTimeOfDay(long timeOfDay, CallbackInfo ci) {
        SetWorldTimeEvent event = new SetWorldTimeEvent();
        Mod.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
