package me.bebeli555.automapart.mixin.render;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.render.Render3DEvent;
import me.bebeli555.automapart.utils.RenderUtils3D;
import me.bebeli555.automapart.utils.Utils;
import me.bebeli555.automapart.utils.objects.MixinParameters;
import me.bebeli555.automapart.utils.render3d.Renderer3D;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Unique private Renderer3D renderer;

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci) {
        if (renderer == null) {
            renderer = new Renderer3D();
        }

        MixinParameters.RENDERED3D_MATRICES = matrices;
        RenderUtils3D.updateScreenCenter();

        Render3DEvent event = Render3DEvent.get(matrices, renderer, tickDelta);
        Mod.EVENT_BUS.post(event);
    }
}
