package me.bebeli555.automapart.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class RenderUtils2D extends Utils {
    /**
     * Draws a circle in 2D
     */
    public static void drawCircle(MatrixStack stack, double x, double y, float radius, int sides, int color) {
        Matrix4f matrix = stack.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) x, (float) y, 0).color(color).next();

        for (int i = 0; i <= sides; i++) {
            double angle = ((Math.PI * 2) * i / sides) + Math.toRadians(180);
            bufferBuilder.vertex(matrix, (float) (x + Math.sin(angle) * radius), (float) (y + Math.cos(angle) * radius), 0).color(color).next();
        }

        tessellator.draw();
    }

    /**
     * Renders an item into the GUI
     */
    public static void renderItemInGui(MatrixStack matrices, ItemStack itemStack, int x, int y) {
        BakedModel model = mc.getItemRenderer().getModel(itemStack, mc.player.getWorld(), mc.player, 0);
        matrices.push();
        matrices.translate(0.0f, 0.0f, 50);

        matrices.push();
        matrices.translate(x, y, 100.0f);
        matrices.translate(8.0f, 8.0f, 0.0f);
        matrices.multiplyPositionMatrix(new Matrix4f().scaling(1.0f, -1.0f, 1.0f));
        matrices.scale(16.0f, 16.0f, 16.0f);
        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        DiffuseLighting.disableGuiDepthLighting();
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
        RenderSystem.applyModelViewMatrix();
        mc.getItemRenderer().renderItem(itemStack, ModelTransformationMode.GUI, false, new MatrixStack(), immediate, 0xF000F0, OverlayTexture.DEFAULT_UV, model);
        immediate.draw();
        RenderSystem.enableDepthTest();
        DiffuseLighting.enableGuiDepthLighting();
        matrices.pop();
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();

        matrices.pop();
    }

    /**
     * Renders the overlay for items in GUIs, including the damage bar and the item count.
     */
    public static void renderGuiItemOverlay(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y, @Nullable String countLabel) {
        if (stack.isEmpty()) {
            return;
        }

        context.getMatrices().push();

        //Durability
        if (stack.isItemBarVisible()) {
            RenderSystem.disableDepthTest();
            int i = stack.getItemBarStep();
            int j = stack.getItemBarColor();
            int k = x + 2;
            int l = y + 13;
            context.getMatrices().translate(0.0f, 0.0f, 200.0f);
            context.fill(k, l, k + 13, l + 2, -16777216);
            context.fill(k, l, k + i, l + 1, j | 0xFF000000);
            RenderSystem.enableDepthTest();
        }

        //Item count
        if (stack.getCount() != 1 || countLabel != null) {
            String string = countLabel == null ? String.valueOf(stack.getCount()) : countLabel;
            context.getMatrices().translate(0.0f, 0.0f, 200.0f);
            VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            textRenderer.draw(string, (float)(x + 19 - 2 - textRenderer.getWidth(string)), (float)(y + 6 + 3), 0xFFFFFF, true, context.getMatrices().peek().getPositionMatrix(), immediate, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            immediate.draw();
        }

        context.getMatrices().pop();
    }
}
