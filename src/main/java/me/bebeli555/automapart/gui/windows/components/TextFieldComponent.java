package me.bebeli555.automapart.gui.windows.components;

import me.bebeli555.automapart.events.game.KeyInputEvent;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.mods.ClientSettings;
import me.bebeli555.automapart.utils.globalsettings.GlobalBorderSettings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TextFieldComponent implements WindowComponent {
    public TitledWindow window;
    public int x, y, width, height;
    public boolean active;
    public boolean onlyRenderText;
    public boolean onlyNumbers;
    public String name, text = "";
    private List<ButtonComponent.ButtonClickListener> listeners = new ArrayList<>();

    public TextFieldComponent(TitledWindow window, String name, int width, int height, boolean onlyRenderText, boolean onlyNumbers) {
        this.window = window;
        this.window.windowComponents.add(this);

        this.name = name;
        this.width = width;
        this.height = height;
        this.onlyRenderText = onlyRenderText;
        this.onlyNumbers = onlyNumbers;
    }

    public TextFieldComponent(TitledWindow window, String name, int width, int height, boolean onlyRenderText) {
        this(window, name, width, height, onlyRenderText, false);
    }

    public TextFieldComponent(TitledWindow window, String name, int width, int height) {
        this(window, name, width, height, false, false);
    }

    public void render(DrawContext context, int x1, int y1) {
        String tempName = name;
        MatrixStack stack = context.getMatrices();
        this.x = window.x + x1 + Gui.fontRenderer.getWidth(stack, name) + 3;
        this.y = window.y + y1;

        if (!onlyRenderText) {
            tempName += ":";

            //Draw background and border
            int c = ClientSettings.titledWindowTextFieldBackground.asInt();
            GlobalBorderSettings.renderWithBackground(ClientSettings.titledWindowTextFieldBorder, stack, x, y, x + width, y + height, active ? getBackgroundSelectedColor(c) : c);

            //Draw string
            Gui.fontRenderer.drawString(stack, text, x + 2, y + (float)height / 2 - (float)Gui.fontRenderer.getHeight(stack) / 3f + 0.5f, ClientSettings.titledWindowTextFieldText.asInt());
        }

        //Draw name string
        Gui.fontRenderer.drawString(stack, tempName, x - Gui.fontRenderer.getWidth(stack, tempName.endsWith(":") ? tempName : tempName + ":") - 3, y + (float)height / 2 - (float)Gui.fontRenderer.getHeight(stack) / 3f + 0.5f, ClientSettings.titledWindowTextFieldName.asInt());
    }

    public static int getBackgroundSelectedColor(int original) {
        Color color = new Color(original, true);
        color = new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(255, color.getAlpha() + 65));
        return color.getRGB();
    }

    @Override
    public void onComponentClick(int mouseX, int mouseY, int button) {
        if (mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height) {
            active = true;

            if (button == 2) {
                text = "";
            }
        } else {
            if (active) {
                for (ButtonComponent.ButtonClickListener listener : listeners) {
                    listener.clicked(button);
                }
            }

            active = false;
        }
    }

    @Override
    public void onComponentChar(char chr) {
        if (active) {
            if (onlyNumbers) {
                if (chr == '.' && text.contains(".")) {
                    return;
                }

                if (chr == '.' && text.isEmpty()) {
                    text += "0";
                }

                if (chr != '-' || !text.isEmpty()) {
                    try {
                        Double.parseDouble("3" + chr + "1");
                    } catch (Exception e) {
                        return;
                    }
                }
            }

            text += chr;
        }
    }

    @Override
    public void onComponentKey(KeyInputEvent event) {
        if (!active) {
            return;
        }

        if (event.getKey() == GLFW.GLFW_KEY_ENTER) {
            active = false;
        } else if (event.getKey() == GLFW.GLFW_KEY_BACKSPACE && text.length() > 0 && (event.getAction() == 1 || event.getAction() == 2)) {
            text = text.substring(0, text.length() - 1);
        }
    }

    public int asInt() {
        return (int)asDouble();
    }

    public double asDouble() {
        try {
            return Double.parseDouble(text);
        } catch (Exception e) {
            return 0;
        }
    }

    public void addClickListener(ButtonComponent.ButtonClickListener listener) {
        this.listeners.add(listener);
    }

    public void addClickListener(Runnable runnable) {
        listeners.add(new ButtonComponent.ButtonClickListener(){
            public void clicked(int button) {
                runnable.run();
            }
        });
    }
}
