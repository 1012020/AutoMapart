package me.bebeli555.automapart.events.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public record RenderGuiEvent(DrawContext context, float tickDelta) {
    public MatrixStack getMatrixStack() {
        return context.getMatrices();
    }

    public record Pre(DrawContext context, float tickDelta) {}
}
