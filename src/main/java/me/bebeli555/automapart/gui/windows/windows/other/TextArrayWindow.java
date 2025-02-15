package me.bebeli555.automapart.gui.windows.windows.other;

import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.gui.GuiNode;
import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.gui.windows.components.ButtonComponent;
import me.bebeli555.automapart.gui.windows.components.TextFieldComponent;
import me.bebeli555.automapart.utils.input.Mouse;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TextArrayWindow extends TitledWindow {
    public static TextArrayWindow INSTANCE;
    public static GuiNode currentNode;

    public TextFieldComponent textField = new TextFieldComponent(this, "Message", 120, 13);
    public ButtonComponent addButton = new ButtonComponent(this, "Add", 10, 25, false);

    public static final String prefix = "\"sc";

    public List<String> list = new ArrayList<>();
    public List<RenderedText> renderedTexts = new ArrayList<>();
    public int scroll = 0;
    public String textSetterS;

    public long pressTime;
    public int pressY;
    public boolean leftDown, rightDown;
    public String holdString;

    public TextArrayWindow() {
        super("Text array window", 200, 300);
        INSTANCE = this;

        addButton.addClickListener(() -> {
            if (!textField.text.isEmpty()) {
                list.add(textField.text);
            }
        });

        TextSetterWindow.INSTANCE.setButton.addClickListener(() -> {
            if (this.isToggled() && textSetterS != null) {
                int index = list.indexOf(textSetterS);
                if (index != -1) {
                    list.set(index, TextSetterWindow.INSTANCE.textField.text);
                }

                textSetterS = null;
            }
        });
    }

    @Override
    public void onEnabled() {
        list = currentNode.setting.asTextArray();
        scroll = 0;
    }

    @Override
    public void onOutsideClick() {
        this.disable();
    }

    @Override
    public void onDisabled() {
        if (currentNode != null) {
            String value = "";
            for (String s : list) {
                if (!s.isEmpty()) {
                    value += s.replace(":", prefix).replace(",", "\\,") + ", ";
                }
            }

            if (!value.isEmpty()) {
                value = value.substring(0, value.length() - 2);
                currentNode.setValue(value);
            }

            currentNode = null;
        }
    }

    @Override
    public void onDraw(DrawContext context, int mouseX, int mouseY, int lastMouseX, int lastMouseY) {
        textField.render(context, 5, 25);
        addButton.render(context, 167, 26);

        String info = "Left click to edit, Right click to remove";
        Gui.fontRenderer.drawString(context.getMatrices(), info, this.x + this.width / 2f - Gui.fontRenderer.getWidth(context.getMatrices(), info) / 2f, this.y + 55, -1);

        info = "Left click and drag to change index";
        Gui.fontRenderer.drawString(context.getMatrices(), info, this.x + this.width / 2f - Gui.fontRenderer.getWidth(context.getMatrices(), info) / 2f, this.y + 65, -1);

        List<RenderedText> temp = new ArrayList<>();
        int index = 3;

        for (String s : list) {
            if (s == null || s.isEmpty()) {
                continue;
            }

            index++;
            int renderY = this.y + 45 + (index * 13) + scroll;
            if (renderY < this.y + 85 || renderY > this.y + this.height - 10) {
                continue;
            }

            RenderedText renderedText = new RenderedText(s, this.x + 10, renderY - 3, this.x + this.width - 10, renderY + 8);
            temp.add(renderedText);

            Gui.drawRect(context.getMatrices(), renderedText.x, renderedText.y, renderedText.x2, renderedText.y2, leftDown && s.equals(holdString) ? new Color(0, 155, 147, 85).getRGB() : new Color(0, 0, 0, 85).getRGB());
            Gui.fontRenderer.drawString(context.getMatrices(), Formatting.GOLD + s, this.x + this.width / 2f - Gui.fontRenderer.getWidth(context.getMatrices(), s) / 2f, renderY, -1);
        }

        renderedTexts = temp;

        long diff = Math.abs(System.currentTimeMillis() - pressTime);
        for (RenderedText renderedText : renderedTexts) {
            if (mouseX >= renderedText.x && mouseX <= renderedText.x2 && mouseY >= renderedText.y && mouseY <= renderedText.y2) {
                if (Mouse.isButtonDown(0) && !leftDown) {
                    leftDown = true;
                    pressY = mouseY;
                    pressTime = System.currentTimeMillis();
                    holdString = renderedText.s;
                } else if (Mouse.isButtonDown(1)) {
                    rightDown = true;
                } else if (leftDown && !Mouse.isButtonDown(0)) {
                    leftDown = false;
                    if (diff < 500) {
                        TextSetterWindow.currentNode = null;
                        TextSetterWindow.INSTANCE.textField.text = renderedText.s;
                        TextSetterWindow.INSTANCE.enable(mouseX, mouseY);
                        textSetterS = renderedText.s;
                    }
                } else if (rightDown && !Mouse.isButtonDown(1)) {
                    rightDown = false;
                    list.remove(renderedText.s);
                }
            }
        }

        if (leftDown && !Mouse.isButtonDown(0)) {
            leftDown = false;
        }

        if (leftDown && diff > 500) {
            int change = (mouseY - pressY) / 14;
            if (change != 0) {
                pressY = mouseY;
                int pressIndex = list.indexOf(holdString);

                if (pressIndex != -1) {
                    if (change > 0 && list.size() > pressIndex + 1) {
                        String tempS = list.get(pressIndex + 1);
                        list.set(pressIndex, tempS);
                        list.set(pressIndex + 1, holdString);
                    } else if (change < 0 && pressIndex - 1 >= 0) {
                        String tempS = list.get(pressIndex - 1);
                        list.set(pressIndex, tempS);
                        list.set(pressIndex - 1, holdString);
                    }
                }
            }
        }
    }

    @Override
    public void onScroll(boolean up, int scroll) {
        this.scroll += up ? scroll : -scroll;
    }

    public record RenderedText(String s, int x, int y, int x2, int y2) {}
}
