package me.bebeli555.automapart.events.player;

public class RotationPacketSend {
    public float yaw, pitch;

    public RotationPacketSend(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
