package me.bebeli555.automapart.hud.components;

import me.bebeli555.automapart.utils.globalsettings.GlobalBorderSettings;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.SettingList;
import me.bebeli555.automapart.settings.SettingValue;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.utils.font.ColorHolder;

public class StopWatchComponent extends HudComponent {
    public static StopWatchComponent INSTANCE;
    public boolean running, reset, pauseCheck;
    public long started, paused;
    public long lastUpdate;

    public static Setting stopWatch = new Setting(Mode.BOOLEAN, "StopWatch", false, "You know, shows passes time from a starting point", "Use stopwatch command to start/stop it", "If you want keybind then set a macro for the command");
        public static Setting scaleSetting = new Setting(stopWatch, Mode.DOUBLE, "Scale", new SettingValue(1, 0.3, 3, 0.1), "Scale for the whole thing");
        public static Setting background = new Setting(stopWatch, Mode.COLOR, "Background", 2097152000, "Background color");
        public static SettingList border = GlobalBorderSettings.get(stopWatch);
        public static Setting nameColor = new Setting(stopWatch, Mode.COLOR, "Name", -1, "Color of the StopWatch: text");
        public static Setting valueColor = new Setting(stopWatch, Mode.COLOR, "Value", -7566196, "Color of the passed time text");


    public StopWatchComponent() {
        super(HudCorner.TOP_LEFT, stopWatch);
        INSTANCE = this;
        this.defaultX = 1;
        this.defaultY = 239;
    }

    @Override
    public void onRender(DrawContext context, float partialTicks) {
        MatrixStack stack = context.getMatrices();

        if (reset) {
            reset = false;
            running = false;
            started = paused = System.currentTimeMillis();
        }

        if (!running) {
            if (pauseCheck) {
                pauseCheck = false;
                paused = System.currentTimeMillis();
            }
        } else {
            if (!pauseCheck) {
                pauseCheck = true;
                started += Math.abs(paused - System.currentTimeMillis());
            }

            if (started == 0) {
                started = System.currentTimeMillis();
            }
        }

        if (running) {
            lastUpdate = Math.abs(System.currentTimeMillis() - started);
        }

        String seconds = decimal((double)lastUpdate / 1000, 2);
        float scale = scaleSetting.asFloat();
        int width = Gui.fontRenderer.getWidth(stack, "Stopwatch: " + seconds + "secs") + 6;
        int height = Gui.fontRenderer.getHeight(stack) + 3;
        double x = getxAdd() / scale;
        double y = getyAdd() / scale;

        stack.push();
        stack.scale(scale, scale, scale);

        //Render background
        Gui.drawRect(stack, x, y, x + width, y + height, background.asInt());

        //Render border
        GlobalBorderSettings.render(border, stack, x, y, x + width, y + height);

        //Render text
        Gui.fontRenderer.drawString(stack, 
                new ColorHolder(nameColor.asInt()) + "Stopwatch: " + new ColorHolder(valueColor.asInt()) + seconds + "secs",
                (float)x + add(),
                (float)y + add() - 0.5f,
                -1
        );

        this.renderedPoints.add(new HudPoint(getxAdd(), getyAdd(), getxAdd() + width * scale, getyAdd() + height * scale));
        stack.pop();
    }
}
