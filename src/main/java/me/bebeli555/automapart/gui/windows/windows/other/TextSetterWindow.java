package me.bebeli555.automapart.gui.windows.windows.other;

import me.bebeli555.automapart.gui.GuiNode;
import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.gui.windows.components.ButtonComponent;
import me.bebeli555.automapart.gui.windows.components.TextFieldComponent;
import net.minecraft.client.gui.DrawContext;

public class TextSetterWindow extends TitledWindow {
    public static TextSetterWindow INSTANCE;
    public static GuiNode currentNode;

    public TextFieldComponent textField = new TextFieldComponent(this, "Text", 200, 11);
    public ButtonComponent setButton = new ButtonComponent(this, "Set");

    public TextSetterWindow() {
        super("Text setter", 250, 30);
        INSTANCE = this;

        setButton.addClickListener(() -> {
            if (currentNode != null) {
                currentNode.setValue(textField.text);
                currentNode.setSetting();
            }

            disable();
        });
    }

    @Override
    public void onDraw(DrawContext context, int mouseX, int mouseY, int lastMouseX, int lastMouseY) {
        textField.render(context, 3, 15);
        setButton.render(context, 230, 16);
    }

    @Override
    public void onOutsideClick() {
        disable();
    }
}
