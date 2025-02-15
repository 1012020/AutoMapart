package me.bebeli555.automapart.mixin.game;

import me.bebeli555.automapart.events.game.ApplyTransformationEvent;
import me.bebeli555.automapart.utils.MixinUtils;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Transformation.class)
public class TransformationMixin {
    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void onApply(boolean leftHanded, MatrixStack matrices, CallbackInfo ci) {
        MixinUtils.basicEvent(new ApplyTransformationEvent((Transformation)(Object)this, leftHanded, matrices), ci);
    }
}
