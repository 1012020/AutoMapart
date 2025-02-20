package me.bebeli555.automapart.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.hud.components.DevPanelComponent;
import me.bebeli555.automapart.mods.ClientSettings;
import me.bebeli555.automapart.utils.font.SierraFontRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Used for rendering 2d things like text to the 3D space
 */
public class Renderer2DIn3D extends Utils {
    public static MatrixStack activeStack;
    public static int statusIconSize = 135;
    public static float lastPerspectiveScale;
    public static Vec3d activePos;
    public static double minDist = 10;
    public static boolean distanceScaleActive;

    public static SierraFontRenderer fontRenderer = new SierraFontRenderer(){
        public int getGap() {return ClientSettings.font3dGap.asInt();}
        public String getFontName() {return ClientSettings.font3dName.string();}
        public String getFontType() {return ClientSettings.font3dType.string();}
        public boolean isShadow() {return ClientSettings.font3dShadow.bool();}
        public int getShadowColor() {return ClientSettings.font3dShadowColor.asInt();}
        public int getShadowOffset() {return ClientSettings.font3dShadowOffset.asInt();}
        public boolean is3d() {return true;}
    };

    public static void begin(MatrixStack stack, Vec3d pos, Entity entity, boolean depth, float tickDelta, boolean face2d, boolean distanceScale, double yScaleDivide) {
        lastPerspectiveScale = 1;
        statusIconSize = 135;
        activeStack = stack;
        distanceScaleActive = distanceScale;

        stack.push();

        if (entity != null) {
            pos = pos.add(EntityUtils.offsetLastTickPos(entity, tickDelta));
        }

        activePos = new Vec3d(pos.x, pos.y, pos.z);
        if (mc.gameRenderer.getCamera().getPos().distanceTo(pos) < minDist) {
            distanceScale = false;
        }

        //Make pos always be x amount of blocks away from camera to keep scale
        if (distanceScale) {
            pos = pos.add(0, (mc.gameRenderer.getCamera().getPos().distanceTo(pos) / 10) / yScaleDivide - (1 / yScaleDivide), 0);

            Vec3d direction = mc.gameRenderer.getCamera().getPos().subtract(pos).normalize();
            pos = mc.gameRenderer.getCamera().getPos().subtract(direction.multiply(10));
        }

        stack.translate(-mc.gameRenderer.getCamera().getPos().x, -mc.gameRenderer.getCamera().getPos().y, -mc.gameRenderer.getCamera().getPos().z);
        stack.translate(pos.x, pos.y, pos.z);

        if (face2d) {
            stack.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(mc.getCameraEntity().getYaw()));
            stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.getCameraEntity().getPitch()));
        } else {
            Vec3d planeToCamera = mc.getCameraEntity().getCameraPosVec(1.0f).subtract(0, 0.9, 0).subtract(pos.x, pos.y, pos.z);
            planeToCamera = planeToCamera.normalize();
            double yaw = Math.atan2(-planeToCamera.getX(), -planeToCamera.getZ()) * (180.0 / Math.PI);
            double pitch = Math.asin(planeToCamera.getY()) * (180.0 / Math.PI);
            stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)yaw));
            stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float)pitch));
        }

        if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
            stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180));
            stack.scale(-1, -1, -1);
        }

        if (depth) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }

        scale(-0.25f);
        scale(0.05f);

        //Scales to avoid perspective rendering
        if (distanceScale || (mc.gameRenderer.getCamera().getPos().distanceTo(pos) < minDist && distanceScaleActive)) {
            Vec3d crosshairPos = mc.gameRenderer.getCamera().getPos().add(mc.getCameraEntity().getRotationVec(tickDelta).normalize().multiply(10));
            float scaledValue = (float)(1 + (crosshairPos.distanceTo(pos) / 20) * -0.5);

            DevPanelComponent.put("PerspectiveDist", scaledValue);
            scale(scaledValue);
            lastPerspectiveScale = scaledValue;
        }
    }

    public static void scale(float scale) {
        activeStack.scale(scale, scale, scale);
    }

    public static void end() {
        if (activeStack != null) {
            activeStack.pop();
            RenderSystem.enableDepthTest();
        }
    }

    public static void renderItem(ItemStack itemStack, int x, int y, boolean depth) {
        MatrixStack matrices = activeStack;

        BakedModel model = mc.getItemRenderer().getModel(itemStack, mc.player.getWorld(), mc.player, 0);
        matrices.push();
        matrices.translate(x, y, 0);
        matrices.multiplyPositionMatrix(new Matrix4f().scaling(1.0f, -1.0f, 1.0f));
        scale(150);

        DiffuseLighting.disableGuiDepthLighting();
        if (!depth) {
            RenderSystem.clear(256, false);
        }

        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
        RenderSystem.applyModelViewMatrix();

        mc.getItemRenderer().renderItem(itemStack, ModelTransformationMode.GUI, false, new MatrixStack(), immediate, 0xF000F0, OverlayTexture.DEFAULT_UV, model);
        immediate.draw();

        matrixStack.pop();
        matrices.pop();

        DiffuseLighting.enableGuiDepthLighting();
        RenderSystem.applyModelViewMatrix();
    }

    public static void renderItemOverlays(ItemStack stack, int x, int y, int color) {
        if (stack.isEmpty()) {
            return;
        }

        activeStack.push();

        //Durability
        if (stack.isItemBarVisible()) {
            activeStack.push();
            float scale = 8.5f;
            scale(scale);

            double durX = x / scale - 6.5;
            double durY = y / scale + 6;

            Gui.drawRect(activeStack, durX, durY, durX + 13, durY + 2, -16777216);
            Gui.drawRect(activeStack, durX, durY, durX + stack.getItemBarStep(), durY + 1, stack.getItemBarColor() | 0xFF000000);
            activeStack.pop();
        }

        //Item count
        if (stack.getCount() != 1) {
            String count = String.valueOf(stack.getCount());
            fontRenderer.drawString(activeStack, count, (x / 2.5f) / SierraFontRenderer.get3dScale(activeStack), (y / 3f) / SierraFontRenderer.get3dScale(activeStack), color);
        }

        activeStack.pop();
    }

    public static void renderStatusIcon(StatusEffect effect, int x, int y) {
        Sprite sprite = mc.getStatusEffectSpriteManager().getSprite(effect);
        new DrawContext(mc, activeStack, null).drawSprite(x - statusIconSize / 2, y - statusIconSize / 2, 0, statusIconSize, statusIconSize, sprite);
    }
}
