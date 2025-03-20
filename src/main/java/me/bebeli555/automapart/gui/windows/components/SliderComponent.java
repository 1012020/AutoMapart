package me.bebeli555.automapart.gui.windows.components;

import me.bebeli555.automapart.events.game.KeyInputEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.mods.ClientSettings;
import me.bebeli555.automapart.utils.input.Mouse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class SliderComponent extends Mod implements WindowComponent {
    public static List<SliderComponent> list = new ArrayList<>();

    public String name;
    public double value, defaultValue, step;
    public double minValue, maxValue;
    public int width, height;
    private boolean dragging, updated;
    public boolean clicked;
    public TitledWindow window;
    private int lastRenderX, lastRenderY, lastSliderX;
    public List<Runnable> listeners = new ArrayList<>();

    public SliderComponent(TitledWindow window, String name, double value, double minValue, double maxValue, double step, int width, int height) {
        this.window = window;
        this.window.windowComponents.add(this);

        this.name = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.width = width;
        this.height = height;
        this.value = value;
        this.defaultValue = this.value;
        this.step = step;
        list.add(this);
    }

    public SliderComponent(TitledWindow window, String name, double minValue, double maxValue, int width, int height) {
        this(window, name, maxValue, minValue, maxValue, 1, width, height);
    }

    public void render(DrawContext context, int x1, int y1) {
        MatrixStack stack = context.getMatrices();

        int x = window.x + x1;
        int y = window.y + y1;

        lastRenderX = x;
        lastRenderY = y;

        //Using slider with mouse
        if (!Mouse.isButtonDown(0)) {
            clicked = false;
        }

        if (((TitledWindow.mouseX >= x && TitledWindow.mouseX <= x + width && TitledWindow.mouseY >= y && TitledWindow.mouseY <= y + height) || dragging) && Mouse.isButtonDown(0) && clicked) {
            dragging = true;

            int check = (int)(Math.abs(minValue - maxValue) / step);
            int amount = Math.abs(TitledWindow.mouseX - lastSliderX);
            if (amount * check > 150) {
                int add = TitledWindow.mouseX - TitledWindow.lastMouseX;
                value += add * step;
                value = new BigDecimal(value).setScale(8, RoundingMode.HALF_UP).doubleValue();

                if (value > maxValue) {
                    value = maxValue;
                } else if (value < minValue) {
                    value = minValue;
                }

                lastSliderX = TitledWindow.mouseX;
                updated = true;

                for (Runnable runnable : listeners) {
                    runnable.run();
                }
            }
        } else {
            dragging = false;
        }

        Gui.drawRect(stack, x, y, x + (int)((double)width / (maxValue / value)), y + height, ClientSettings.titledWindowSliderBackground.asInt());

        String sValue = "" + value;
        if (sValue.endsWith(".0")) {
            sValue = sValue.replace(".0", "");
        }

        Gui.fontRenderer.drawString(stack, name, x + 2, y + height / 2f - (Gui.fontRenderer.getHeight(stack) / 3f), ClientSettings.titledWindowSliderText.asInt());
        Gui.fontRenderer.drawString(stack, sValue, x + width - 2 - Gui.fontRenderer.getWidth(stack, sValue), y + height / 2f - (Gui.fontRenderer.getHeight(stack) / 3f), ClientSettings.titledWindowSliderText.asInt());
    }

    @Override
    public void onComponentClick(int mouseX, int mouseY, int button) {
        if (((TitledWindow.mouseX >= lastRenderX && TitledWindow.mouseX <= lastRenderX + width && TitledWindow.mouseY >= lastRenderY && TitledWindow.mouseY <= lastRenderY + height))) {
            if (button == 2) {
                this.value = this.defaultValue;
                for (Runnable runnable : listeners) {
                    runnable.run();
                }

                updated = true;
            } else {
                clicked = true;
            }
        }
    }

    public boolean hasUpdated() {
        if (updated) {
            updated = false;
            return true;
        }

        return false;
    }

    public void addValueChangedListener(Runnable runnable) {
        this.listeners.add(runnable);
    }

    public void onComponentChar(char chr) {}
    public void onComponentKey(KeyInputEvent event) {}
}
