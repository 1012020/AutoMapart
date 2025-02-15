package me.bebeli555.automapart.events.game;

import me.bebeli555.automapart.events.Cancellable;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;

public class ApplyTransformationEvent extends Cancellable {
    public Transformation transformation;
    public boolean leftHanded;
    public MatrixStack matrices;

    public ApplyTransformationEvent(Transformation transformation, boolean leftHanded, MatrixStack matrices) {
        this.transformation = transformation;
        this.leftHanded = leftHanded;
        this.matrices = matrices;
    }
}
