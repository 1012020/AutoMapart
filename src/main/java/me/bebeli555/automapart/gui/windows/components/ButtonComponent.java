package me.bebeli555.automapart.gui.windows.components;

import me.bebeli555.automapart.events.game.KeyInputEvent;
import me.bebeli555.automapart.utils.globalsettings.GlobalBorderSettings;
import me.bebeli555.automapart.utils.input.Mouse;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.mods.ClientSettings;

import java.util.ArrayList;
import java.util.List;

public class ButtonComponent extends Mod implements WindowComponent {
    public static List<ButtonComponent> list = new ArrayList<>();

    public TitledWindow window;
    public String name;
    public int width, height;
    public int customHeight, customWidth;
    public int x, y;
    public boolean down, customDown;
    public boolean renderOnlyText, dontRender;
    public Object customObject;

    public List<ButtonClickListener> listeners = new ArrayList<>();

    public ButtonComponent(TitledWindow window, String name, int customHeight, int customWidth, boolean renderOnlyText) {
        this.window = window;
        this.window.windowComponents.add(this);

        this.name = name;
        this.customHeight = customHeight;
        this.customWidth = customWidth;
        this.renderOnlyText = renderOnlyText;

        list.add(this);
    }

    public ButtonComponent(TitledWindow window, String name, int customHeight, boolean renderOnlyText) {
        this(window, name, customHeight, 0, renderOnlyText);
    }

    public ButtonComponent(TitledWindow window, String name, boolean renderOnlyText) {
        this(window, name, 0, renderOnlyText);
    }

    public ButtonComponent(TitledWindow window, String name, int customHeight) {
        this(window, name, customHeight, false);
    }

    public ButtonComponent(TitledWindow window, String name) {
        this(window, name, 0, false);
    }

    /**
     * Render centered
     */
    public void render(DrawContext context, int y) {
        int width = (int)(Gui.fontRenderer.getWidth(context.getMatrices(), name) + ClientSettings.titledWindowButtonSize.asDouble() * 2.25);
        render(context, window.width / 2 - width / 2, y);
    }

    public void render(DrawContext context, int x1, int y1) {
        if (this.dontRender) {
            return;
        }

        MatrixStack stack = context.getMatrices();
        if (!Mouse.isButtonDown(0)) {
            down = false;
        }

        this.x = window.x + x1;
        this.y = window.y + y1;
        this.width = customWidth != 0 ? customWidth : Gui.fontRenderer.getWidth(stack, name);
        this.height = customHeight != 0 ? customHeight : Gui.fontRenderer.getHeight(stack);

        //Draw background and border
        if (!renderOnlyText) {
            double o = ClientSettings.titledWindowButtonSize.asDouble();
            int c = ClientSettings.titledWindowButtonBackground.asInt();
            GlobalBorderSettings.renderWithBackground(ClientSettings.titledWindowButtonBorder, stack, x - o, y - o / 2, x + width + o * 1.25, y + height + o / 1.65, (down || customDown) ? TextFieldComponent.getBackgroundSelectedColor(c) : c);
        }

        int textX = this.x;
        if (customWidth != 0) {
            textX += width / 2 - Gui.fontRenderer.getWidth(stack, name) / 2;
        }

        //Draw string
        Gui.fontRenderer.drawString(stack, name, textX, y + (float)height / 2 - (float)Gui.fontRenderer.getHeight(stack) / 2 + 2, ClientSettings.titledWindowButtonText.asInt());
    }

    @Override
    public void onComponentClick(int mouseX, int mouseY, int button) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height && !dontRender) {
            if (button == 0) {
                down = true;
            }

            for (ButtonClickListener listener : listeners) {
                listener.clicked(button);
            }
        }
    }

    public void addClickListener(ButtonClickListener listener) {
        listeners.add(listener);
    }

    public void addClickListener(Runnable runnable) {
        listeners.add(new ButtonClickListener(){
           public void clicked(int button) {
               runnable.run();
           }
        });
    }

    public static class ButtonClickListener {
        public void clicked(int button) {}
    }

    public void onComponentChar(char chr) {}
    public void onComponentKey(KeyInputEvent event) {}
}
