package me.bebeli555.automapart.mixin.render;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.game.TimerEvent;
import me.bebeli555.automapart.utils.Utils;
import me.bebeli555.automapart.utils.objects.MixinParameters;
import net.minecraft.client.render.RenderTickCounter;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.class)
public class RenderTickCounterMixin {
    @Shadow public float lastFrameDuration;
    @Shadow public float tickDelta;

    @Inject(method = "beginRenderTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter;prevTimeMillis:J", opcode = Opcodes.PUTFIELD))
    private void onBeingRenderTick(long a, CallbackInfoReturnable<Integer> info) {
        if (Utils.mc.player == null) {
            return;
        }

        TimerEvent event = new TimerEvent();
        Mod.EVENT_BUS.post(event);

        MixinParameters.tickPhase = MixinParameters.TickPhase.DEFAULT;
        if (event.onlyMovement) {
            float dur = lastFrameDuration * (float)(event.tps / 20);
            float delta = this.tickDelta;
            delta += dur;
            int i = (int)delta;

            MixinParameters.tickPhase = MixinParameters.TickPhase.ONLY_PLAYER;
            for (int j = 0; j < Math.min(10, i); ++j) {
                Utils.mc.world.tickEntities();
            }

            MixinParameters.tickPhase = MixinParameters.TickPhase.NO_PLAYER;
        } else {
            lastFrameDuration *= event.tps / 20;
        }
    }
}