package me.bebeli555.automapart.events.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;

public class RenderHeldItemEvent {
    public MatrixStack matrices;
    public Hand hand;

    public RenderHeldItemEvent(MatrixStack matrices, Hand hand) {
        this.matrices = matrices;
        this.hand = hand;
    }
}
