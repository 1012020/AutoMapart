package me.bebeli555.automapart.events.render;

import me.bebeli555.automapart.events.Cancellable;
import net.minecraft.client.render.Camera;

public class SetCameraClipEvent extends Cancellable {
    public Camera camera;
    public double distance;

    public SetCameraClipEvent(Camera camera, double distance) {
        this.camera = camera;
        this.distance = distance;
    }
}
