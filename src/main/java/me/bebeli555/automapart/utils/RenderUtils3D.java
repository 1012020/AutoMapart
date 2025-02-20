package me.bebeli555.automapart.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class RenderUtils3D extends Utils {
    private static Vec3d center;
    private static Vec3d centerMainPlayer;

    /**
     * Gets the center position in 3D world
     */
    public static Vec3d getCenterPos() {
        return center;
    }

    /**
     * Gets a center position for mc.player
     */
    public static Vec3d getCenterMainPlayerPos() {
        return centerMainPlayer;
    }

    /**
     * Updates the screen center which can be used to render stuff without bobbing effect
     */
    public static void updateScreenCenter() {
        Vector3f pos = new Vector3f(0, 0, 1);

        if (mc.options.getBobView().getValue()) {
            MatrixStack bobViewMatrices = new MatrixStack();

            bobView(bobViewMatrices);
            pos.mulPosition(bobViewMatrices.peek().getPositionMatrix().invert());
        }

        center = new Vec3d(pos.x, -pos.y, pos.z)
                .rotateX(-(float) Math.toRadians(mc.gameRenderer.getCamera().getPitch()))
                .rotateY(-(float) Math.toRadians(mc.gameRenderer.getCamera().getYaw()))
                .add(mc.gameRenderer.getCamera().getPos());

        centerMainPlayer = mc.player.getPos().add(0, mc.player.getStandingEyeHeight(), 0);
    }

    /**
     * Gets normalized vector of the crosshair direction
     */
    public static Vec3d getPlayerFacingDirection() {
        return new Vec3d(10, 10, 10)
                .rotateX(-(float) Math.toRadians(mc.gameRenderer.getCamera().getPitch()))
                .rotateY(-(float) Math.toRadians(mc.gameRenderer.getCamera().getYaw()))
                .normalize();
    }

    /**
     * Minecraft's bob view method
     */
    private static void bobView(MatrixStack matrices) {
        Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();

        if (cameraEntity instanceof PlayerEntity playerEntity) {
            float f = MinecraftClient.getInstance().getTickDelta();
            float g = playerEntity.horizontalSpeed - playerEntity.prevHorizontalSpeed;
            float h = -(playerEntity.horizontalSpeed + g * f);
            float i = MathHelper.lerp(f, playerEntity.prevStrideDistance, playerEntity.strideDistance);

            matrices.translate(-(MathHelper.sin(h * 3.1415927f) * i * 0.5), Math.abs(MathHelper.cos(h * 3.1415927f) * i), 0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin(h * 3.1415927f) * i * 3));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(Math.abs(MathHelper.cos(h * 3.1415927f - 0.2f) * i) * 5));
        }
    }

    /**
     * Offsets the positions with tickdelta so its rendered smoothly
     */
    public static Vec3d offsetLastTickPos(Vec3d pos, Vec3d prevPos, float tickDelta) {
        return new Vec3d(
                (pos.x - prevPos.x) * tickDelta - (pos.x - prevPos.x),
                (pos.y - prevPos.y) * tickDelta - (pos.y - prevPos.y),
                (pos.z - prevPos.z) * tickDelta - (pos.z - prevPos.z)
        );
    }
}
