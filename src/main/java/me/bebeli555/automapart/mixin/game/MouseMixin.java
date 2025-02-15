package me.bebeli555.automapart.mixin.game;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.game.GetMouseSensitivityEvent;
import me.bebeli555.automapart.events.game.MouseInputEvent;
import me.bebeli555.automapart.utils.MixinUtils;
import me.bebeli555.automapart.utils.Utils;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(method = "onMouseButton", at = @At("RETURN"))
    private void onOnMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        MixinUtils.basicEvent(new MouseInputEvent(button, action), ci);

        if (button == 2) {
            MixinUtils.basicEvent(new MouseInputEvent.MiddleClick(), ci);
        }
    }

    @Inject(method = "onMouseScroll", at = @At("RETURN"))
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MixinUtils.basicEvent(new MouseInputEvent.Scroll(horizontal, vertical), ci);
    }

    @Redirect(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"))
    private void changeLookDirection(ClientPlayerEntity player, double x, double y) {
        player.changeLookDirection(x, y);
    }

    @Redirect(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;", ordinal = 0))
    private <T>T updateMouseGetMouseSensitivity(SimpleOption<Double> instance) {
        GetMouseSensitivityEvent event = new GetMouseSensitivityEvent(instance.getValue());
        Mod.EVENT_BUS.post(event);

        return (T)(Double)event.value;
    }
}
