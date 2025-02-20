package me.bebeli555.automapart.mixin.game;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.game.KeyInputEvent;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("RETURN"))
    public void onOnKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        KeyInputEvent event = new KeyInputEvent(key, scancode, action, modifiers);
        Mod.EVENT_BUS.post(event);
    }
}
