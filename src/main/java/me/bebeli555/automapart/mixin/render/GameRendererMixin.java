package me.bebeli555.automapart.mixin.render;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.game.GetFovEvent;
import me.bebeli555.automapart.events.render.WorldPostRenderEvent;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @ModifyVariable(method = "getFov", index = 4, at = @At(value = "STORE", ordinal = 1))
    private double getFov(double value) {
        GetFovEvent event = new GetFovEvent(value);
        Mod.EVENT_BUS.post(event);

        return event.fov;
    }

    @Inject(method = "renderWorld", at = @At("TAIL"))
    private void onRenderWorldTail(CallbackInfo info) {
        Mod.EVENT_BUS.post(new WorldPostRenderEvent());
    }
}
