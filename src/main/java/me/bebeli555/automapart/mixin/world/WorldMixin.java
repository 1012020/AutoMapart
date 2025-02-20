package me.bebeli555.automapart.mixin.world;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.block.BlockStateChangedEvent;
import me.bebeli555.automapart.utils.Utils;
import me.bebeli555.automapart.utils.objects.MixinParameters;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(World.class)
public class WorldMixin <T extends Entity> {
    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At("RETURN"))
    private void setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        Mod.EVENT_BUS.post(new BlockStateChangedEvent(pos, state));
    }

    @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    private void tickEntity(Consumer<T> tickConsumer, T entity, CallbackInfo ci) {
        if (MixinParameters.tickPhase == MixinParameters.TickPhase.NO_PLAYER && entity == Utils.mc.player) {
            ci.cancel();
        } else if (MixinParameters.tickPhase == MixinParameters.TickPhase.ONLY_PLAYER && entity != Utils.mc.player) {
            ci.cancel();
        }
    }
}
