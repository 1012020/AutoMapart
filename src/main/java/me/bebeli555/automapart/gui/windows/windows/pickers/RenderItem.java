package me.bebeli555.automapart.gui.windows.windows.pickers;

import com.mojang.blaze3d.systems.RenderSystem;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.utils.RenderUtils2D;
import net.minecraft.block.Block;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;

public class RenderItem extends Mod {
    public Type type;
    public Object object;

    public RenderItem(Object object) {
        this.object = object;

        if (object instanceof Item || object instanceof ItemStack) {
            this.type = Type.ITEM;
        } else if (object instanceof Block) {
            this.type = Type.BLOCK;
        } else if (object instanceof StatusEffect) {
            this.type = Type.EFFECT;
        } else if (object instanceof Entity) {
            this.type = Type.ENTITY;
        }
    }

    public void render(DrawContext context, int x, int y) {
        if (type == Type.BLOCK) {
            Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            Sprite sprite = mc.getBlockRenderManager().getModels().getModelParticleSprite(((Block)object).getDefaultState());
            float f = sprite.getMinU();
            float f1 = sprite.getMinV();
            float f2 = sprite.getMaxU();
            float f3 = sprite.getMaxV();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
            int renderX = x + 12;
            int renderY = y - 3;
            bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            bufferbuilder.vertex(matrix, renderX, renderY + 15, -90.0f).texture(f, f3).next();
            bufferbuilder.vertex(matrix, renderX + 15, renderY + 15, -90.0f).texture(f2, f3).next();
            bufferbuilder.vertex(matrix, renderX + 15, renderY, -90.0f).texture(f2, f1).next();
            bufferbuilder.vertex(matrix, renderX, renderY, -90.0f).texture(f, f1).next();
            tessellator.draw();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else if (type == Type.EFFECT) {
            Sprite sprite = mc.getStatusEffectSpriteManager().getSprite((StatusEffect)object);
            context.drawSprite(x + 12, y - 3, 0, 15, 15, sprite);
        } else if (type == Type.ITEM) {
            ItemStack itemStack;
            if (object instanceof ItemStack) {
                itemStack = (ItemStack)object;
            } else {
                itemStack = new ItemStack((Item)object);
            }

            RenderUtils2D.renderItemInGui(context.getMatrices(), itemStack, x + 12, y - 3);
        } else if (type == Type.ENTITY && object instanceof LivingEntity entity) {
            EntityDimensions dimensions = entity.getDimensions(entity.getPose());

            int yAdd = (int)(8 + dimensions.height * 4);
            int size = (int)(10 / (dimensions.height + dimensions.width)) + 5;
            if (entity instanceof EnderDragonEntity) {
                size = 2;
                yAdd = 10;
            } else if (entity instanceof GhastEntity) {
                size = 2;
                yAdd = 8;
            } else if (entity instanceof GiantEntity) {
                size = 2;
                yAdd = 18;
            } else if (entity instanceof SquidEntity) {
                yAdd -= 7;
            }

            Gui.drawRect(context.getMatrices(), 0, 0, 0, 0, -1);
        }
    }

    public enum Type {
        BLOCK,
        ITEM,
        EFFECT,
        ENTITY
    }
}
