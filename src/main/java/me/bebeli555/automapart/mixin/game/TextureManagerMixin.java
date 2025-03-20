package me.bebeli555.automapart.mixin.game;

import me.bebeli555.automapart.gui.windows.windows.tools.TextureEditorTool;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {
    @Shadow @Final public Map<Identifier, AbstractTexture> textures;
    @Shadow protected abstract AbstractTexture loadTexture(Identifier id, AbstractTexture texture);
    @Shadow public abstract void registerTexture(Identifier id, AbstractTexture texture);

    @Unique private Map<AbstractTexture, AbstractTexture> changedTextures = new HashMap<>();
    @Unique private boolean check;

    @Inject(method = "registerTexture", at = @At("HEAD"), cancellable = true)
    private void registerTexture(Identifier id, AbstractTexture passedTexture, CallbackInfo ci) {
        if (check) {
            check = false;
            return;
        }

        if (!id.getPath().startsWith("dynamic")) {
            try {
                TextureEditorTool.EditedTexture texture = TextureEditorTool.editedTextures.stream().filter(t -> t.path().equals(id.getPath())).findFirst().orElse(null);
                if (texture != null) {
                    ci.cancel();

                    NativeImageBackedTexture aTexture = new NativeImageBackedTexture(NativeImage.read(texture.data()));
                    this.textures.put(id, this.loadTexture(id, aTexture));

                    changedTextures.put(aTexture, passedTexture);
                } else if (changedTextures.containsKey(passedTexture)) {
                    ci.cancel();

                    check = true;
                    this.registerTexture(id, changedTextures.get(passedTexture));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
