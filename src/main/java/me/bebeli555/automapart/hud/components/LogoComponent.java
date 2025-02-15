package me.bebeli555.automapart.hud.components;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.SettingValue;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class LogoComponent extends HudComponent {
    public Identifier identifier = new Identifier(Mod.MOD_ID, "logo.png");

    public static Setting logo = new Setting(Mode.BOOLEAN, "Logo", false, "Shows " + NAME + " logo");
        public static Setting logoWidth = new Setting(logo, Mode.INTEGER, "Width", new SettingValue(10, 1, 100), "Rendered width");
        public static Setting logoHeight = new Setting(logo, Mode.INTEGER, "Height", new SettingValue(13, 1, 100), "Rendered height");

    public LogoComponent() {
        super(HudCorner.TOP_LEFT, logo);
        this.defaultX = 148;
        this.defaultY = 161;
    }

    @Override
    public void onRender(DrawContext context, float partialTicks) {
        int width = logoWidth.asInt();
        int height = logoHeight.asInt();

        context.drawTexture(identifier, (int)getxAdd() - width, (int)getyAdd() - height, 0, 0, width * 2, height * 2, width * 2, height * 2);
        this.renderedPoints.add(new HudPoint((int) getxAdd() - width, (int) getyAdd() - height, (int) getxAdd() + width, (int) getyAdd() + height));
    }
}
