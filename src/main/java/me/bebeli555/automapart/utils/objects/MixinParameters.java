package me.bebeli555.automapart.utils.objects;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.math.Vec3d;

public class MixinParameters {
    public static ResourceFactory resourceFactory;
    public static MatrixStack RENDERED3D_MATRICES;
    public static TickPhase tickPhase = TickPhase.DEFAULT;
    public static Vec3d timerPos, timerPrevPos;

    public enum TickPhase {
        ONLY_PLAYER(),
        NO_PLAYER(),
        DEFAULT()
    }
}
