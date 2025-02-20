package me.bebeli555.automapart.hud.components;

import com.google.common.eventbus.Subscribe;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.game.MouseInputEvent;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.utils.globalsettings.GlobalBorderSettings;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.SettingList;
import me.bebeli555.automapart.settings.SettingValue;
import me.bebeli555.automapart.utils.objects.Timer;
import me.bebeli555.automapart.utils.input.Mouse;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public class KeyStrokesComponent extends HudComponent {
    public List<Long> clicks = new ArrayList<>();
    public Timer cpsTimer = new Timer();
    public int cps;

    public static Setting keyStrokes = new Setting(Mode.BOOLEAN, "KeyStrokes", false, "Show key/mouse strokes and CPS");
        public static Setting scaleSetting = new Setting(keyStrokes, Mode.DOUBLE, "Scale", new SettingValue(1, 0.1, 3, 0.1), "Scale of this component");
        public static SettingList border = GlobalBorderSettings.get(keyStrokes, false, true, -16777216);
        public static Setting background = new Setting(keyStrokes, Mode.COLOR, "Background", 1677721602, "Background color");
        public static Setting backgroundOn = new Setting(keyStrokes, Mode.COLOR, "BackgroundON", -927399936, "Background color when input is active");
        public static Setting textColor = new Setting(keyStrokes, Mode.COLOR, "TextColor", -1, "Color of the rendered texts");
        public static Setting sizeSetting = new Setting(keyStrokes, Mode.INTEGER, "Size", new SettingValue(20, 5, 50, 1));
        public static Setting gapSetting = new Setting(keyStrokes, Mode.INTEGER, "Gap", new SettingValue(2, 0, 15, 1));
        public static Setting WASD = new Setting(keyStrokes, Mode.BOOLEAN, "WASD", true);
        public static Setting mouseButtons = new Setting(keyStrokes, Mode.BOOLEAN, "MouseButtons", true);
        public static Setting sprint = new Setting(keyStrokes, Mode.BOOLEAN, "Sprint", true);
        public static Setting sneak = new Setting(keyStrokes, Mode.BOOLEAN, "Sneak", true);
        public static Setting jump = new Setting(keyStrokes, Mode.BOOLEAN, "Jump", false);
        public static Setting CPS = new Setting(keyStrokes, Mode.BOOLEAN, "CPS", true);

    public KeyStrokesComponent() {
        super(HudCorner.TOP_LEFT, keyStrokes);
        Mod.EVENT_BUS.register(this);
        this.defaultY = 7;
        this.defaultX = 137;
    }

    @Override
    public void onRender(DrawContext context, float partialTicks) {
        MatrixStack stack = context.getMatrices();

        float scale = scaleSetting.asFloat();
        stack.push();
        stack.scale(scale, scale, scale);

        List<Object[]> list = new ArrayList<>();

        int size = sizeSetting.asInt();
        int gap = gapSetting.asInt();
        int y = 0;

        if (WASD.bool()) {
            list.add(new Object[]{0, 0, size, size, "W", mc.options.forwardKey.isPressed()});
            y += size + gap;
            list.add(new Object[]{-size -gap, size + gap, size, size, "A", mc.options.leftKey.isPressed()});
            list.add(new Object[]{0, size + gap, size, size, "S", mc.options.backKey.isPressed()});
            list.add(new Object[]{size + gap, size + gap, size, size, "D", mc.options.rightKey.isPressed()});
        }

        if (mouseButtons.bool()) {
            y += size + gap;
            list.add(new Object[]{-size - gap, y, (int)(size * 1.5) + gap / 2, size, "RMB", Mouse.isButtonDown(0)});
            list.add(new Object[]{(-size - gap) + (int)(size * 1.5) + gap * 2 - gap / 2, y, (int)(size * 1.5) + gap / 2, size, "LMB", Mouse.isButtonDown(1)});
        }

        if (sprint.bool()) {
            y += size + gap;
            list.add(new Object[]{-size -gap, y, size * 3 + gap * 2, size, "Sprint", mc.options.sprintKey.isPressed()});
        }

        if (sneak.bool()) {
            y += size + gap;
            list.add(new Object[]{-size -gap, y, size * 3 + gap * 2, size, "Sneak", mc.options.sneakKey.isPressed()});
        }

        if (jump.bool()) {
            y += size + gap;
            list.add(new Object[]{-size -gap, y, size * 3 + gap * 2, size, "Jump", mc.options.jumpKey.isPressed()});
        }

        if (CPS.bool()) {
            if (cpsTimer.hasPassed(100)) {
                cps = 0;
                cpsTimer.reset();
                List<Long> copy = new ArrayList<>(clicks);

                for (long click : copy) {
                    if (Math.abs(System.currentTimeMillis() - click) <= 1000) {
                        cps++;
                    } else {
                        clicks.remove(click);
                    }
                }
            }

            y += size + gap;
            list.add(new Object[]{-size -gap, y, size * 3 + gap * 2, size, "CPS: " + (cps / 2), false});
        }

        for (Object[] object : list) {
            renderItem(stack, (int)object[0], (int)object[1], (int)object[2], (int)object[3], (String)object[4], (boolean)object[5]);
        }

        stack.pop();
    }

    public void renderItem(MatrixStack stack, int x, int y, int width, int height, String text, boolean active) {
        //Change coords with adds
        double scale = scaleSetting.asDouble();
        x += getxAdd() / scale;
        y += getyAdd() / scale;
        int x2 = x + width;
        int y2 = y + height;

        //Draw background
        Gui.drawRect(stack, x, y, x2, y2, active ? backgroundOn.asInt() : background.asInt());
        this.renderedPoints.add(new HudPoint(x * scale, y * scale, x2 * scale, y2 * scale));

        //Draw border
        GlobalBorderSettings.render(border, stack, x, y, x2, y2);

        //Render text
        Gui.fontRenderer.drawString(stack, 
                text,
                (float)((x + (double)width / 2) - (float)Gui.fontRenderer.getWidth(stack, text) / 2),
                (float)((y + (double)height / 2) - (float)Gui.fontRenderer.getHeight(stack) / 3),
                textColor.asInt()
        );
    }

    @Subscribe
    private void onMouse(MouseInputEvent e) {
        if (e.getButton() == 0) {
            clicks.add(System.currentTimeMillis());
        }
    }
}
