package me.bebeli555.automapart.hud.components;

import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.SettingValue;
import me.bebeli555.automapart.utils.objects.Timer;
import me.bebeli555.automapart.utils.font.SierraFontRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;

public class PlayerModelComponent extends HudComponent {
    public PlayerEntity entity;
    public Timer rayTimer = new Timer();
    public long looked;

    public static Setting playerModel = new Setting(Mode.BOOLEAN, "PlayerModel", false, "Show your or others player models");
        public static Setting onLook = new Setting(playerModel, Mode.BOOLEAN, "OnLook", true, "Show playermodel of the player you look at");
            public static Setting onLookKeep = new Setting(onLook, Mode.INTEGER, "Keep", new SettingValue(5, 1, 250, 1), "How long to keep it in seconds");
        public static Setting scaleSetting = new Setting(playerModel, Mode.INTEGER, "Scale", new SettingValue(35, 5, 100, 1), "Model scale");

    public PlayerModelComponent() {
        super(HudCorner.TOP_LEFT, playerModel);
        this.defaultX = 202;
        this.defaultY = 48;
    }

    @Override
    public void onRender(DrawContext context, float partialTicks) {
        MatrixStack stack = context.getMatrices();

        //Get entity
        if (entity == null) {
            entity = mc.player;
        }

        if (rayTimer.hasPassed(250)) {
            rayTimer.reset();
            if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY && onLook.bool()) {
                Entity check = ((EntityHitResult)mc.crosshairTarget).getEntity();
                if (check instanceof PlayerEntity) {
                    entity = (PlayerEntity) ((EntityHitResult)mc.crosshairTarget).getEntity();
                    looked = System.currentTimeMillis();
                }
            }
        }

        if (Math.abs(System.currentTimeMillis() - looked) / 1000 > onLookKeep.asInt()) {
            entity = mc.player;
        }

        //Render entity
        float stackScale = (float)SierraFontRenderer.getScale(stack);
        int scale = (int)(scaleSetting.asInt() * stackScale);

        Gui.drawRect(stack, 0, 0, 0, 0, -1);

        this.renderedPoints.add(new HudPoint(getxAdd(), getyAdd(), getxAdd() + scale, getyAdd() + 5 + scale * 2));
    }
}
