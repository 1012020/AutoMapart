package me.bebeli555.automapart.events.render;

import me.bebeli555.automapart.utils.render3d.Renderer3D;
import net.minecraft.client.util.math.MatrixStack;

public class Render3DEvent {
    private static final Render3DEvent INSTANCE = new Render3DEvent();

    public MatrixStack matrices;
    public Renderer3D renderer;
    public float tickDelta;

    public static Render3DEvent get(MatrixStack matrices, Renderer3D renderer, float tickDelta) {
        INSTANCE.matrices = matrices;
        INSTANCE.renderer = renderer;
        INSTANCE.tickDelta = tickDelta;
        return INSTANCE;
    }
}
