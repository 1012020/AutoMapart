package me.bebeli555.automapart.mixin.world;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.render.SetCameraClipEvent;
import me.bebeli555.automapart.utils.Utils;
import me.bebeli555.automapart.utils.objects.MixinParameters;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public class CameraMixin {
    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void setPos(Camera instance, double x, double y, double z) {
        float tickDelta = Utils.mc.getTickDelta();
        Vec3d prevPos = new Vec3d(instance.getFocusedEntity().prevX, instance.getFocusedEntity().prevY, instance.getFocusedEntity().prevZ);
        Vec3d pos = instance.getFocusedEntity().getPos();
        if (MixinParameters.timerPrevPos != null && MixinParameters.timerPos != null && MixinParameters.tickPhase == MixinParameters.TickPhase.NO_PLAYER) {
            prevPos = MixinParameters.timerPrevPos;
            pos = MixinParameters.timerPos;
        }

        instance.setPos(MathHelper.lerp(tickDelta, prevPos.x, pos.x), MathHelper.lerp(tickDelta, prevPos.y, pos.y) + (double)MathHelper.lerp(tickDelta, instance.lastCameraY, instance.cameraY), MathHelper.lerp(tickDelta, prevPos.z, pos.z));
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;clipToSpace(D)D"))
    private double clipToSpace(Camera camera, double desiredCameraDistance) {
        SetCameraClipEvent event = new SetCameraClipEvent(camera, desiredCameraDistance);
        Mod.EVENT_BUS.post(event);

        if (event.isCancelled()) {
            return event.distance;
        }

        return camera.clipToSpace(event.distance);
    }
}
