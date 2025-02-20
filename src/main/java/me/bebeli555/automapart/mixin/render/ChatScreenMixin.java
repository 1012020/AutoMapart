package me.bebeli555.automapart.mixin.render;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.game.ChatScreenKeyEvent;
import me.bebeli555.automapart.utils.Utils;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        ChatScreenKeyEvent event = new ChatScreenKeyEvent();
        Mod.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getTextBackgroundColor(I)I"))
    private int renderBackground(GameOptions instance, int fallbackColor) {
        return Utils.mc.options.getTextBackgroundColor(fallbackColor);
    }
}
