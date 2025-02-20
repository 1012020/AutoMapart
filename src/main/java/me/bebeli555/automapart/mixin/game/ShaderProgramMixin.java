package me.bebeli555.automapart.mixin.game;

import me.bebeli555.automapart.utils.objects.MixinParameters;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.ResourceFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShaderProgram.class)
public class ShaderProgramMixin {
    @Inject(method = "<init>(Lnet/minecraft/resource/ResourceFactory;Ljava/lang/String;Lnet/minecraft/client/render/VertexFormat;)V", at = @At("RETURN"))
    public void init(ResourceFactory factory, String name, VertexFormat format, CallbackInfo ci) {
        if (MixinParameters.resourceFactory == null) {
            MixinParameters.resourceFactory = factory;
        }
    }
}
