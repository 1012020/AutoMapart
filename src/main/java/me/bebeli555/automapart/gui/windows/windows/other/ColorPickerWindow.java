package me.bebeli555.automapart.gui.windows.windows.other;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.gui.GuiNode;
import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.gui.windows.components.ButtonComponent;
import me.bebeli555.automapart.gui.windows.components.SliderComponent;
import me.bebeli555.automapart.utils.input.Mouse;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ColorPickerWindow extends TitledWindow {
    public static ColorPickerWindow INSTANCE;

    public Identifier identifier = new Identifier(Mod.MOD_ID, "colors.jpg");
    public SliderComponent r = new SliderComponent(this, "Red", 0, 255, 80, 10);
    public SliderComponent g = new SliderComponent(this, "Green", 0, 255, 80, 10);
    public SliderComponent b = new SliderComponent(this, "Blue", 0, 255, 80, 10);
    public SliderComponent a = new SliderComponent(this, "Alpha", 0, 255, 80, 10);

    public ButtonComponent applyButton = new ButtonComponent(this, "Apply", 10, 32, false);
    public ButtonComponent resetButton = new ButtonComponent(this, "Reset", 10, 32, false);

    public static @Nullable GuiNode currentNode;
    public int previousColor;
    public Color resetColor;
    public BufferedImage image = ImageIO.read(ColorPickerWindow.class.getResourceAsStream("/assets/automapart/colors.jpg"));

    public ColorPickerWindow() throws IOException {
        super("ColorPicker", 200, 80);
        INSTANCE = this;

        resetButton.addClickListener(() -> {
            if (currentNode != null) {
                currentNode.setValue("" + currentNode.defaultValue);
                setColors(Integer.parseInt(currentNode.defaultValue));
            } else if (resetColor != null) {
                setColors(resetColor.getRGB());
            }
        });

        applyButton.addClickListener(() -> disable());
    }

    @Override
    public void onEnabled() {
        resetColor = null;
        if (currentNode != null) {
            previousColor = currentNode.setting.asInt();
            setColors(previousColor);
        }
    }

    @Override
    public void onOutsideClick() {
        if (currentNode != null) currentNode.setValue("" + previousColor);
        disable();
    }

    @Override
    public void onDisabled() {
        currentNode = null;
    }

    @Override
    public void onDraw(DrawContext context, int mouseX, int mouseY, int lastMouseX, int lastMouseY) {
        //Render color spectrum
        context.drawTexture(identifier, this.x + 3, this.y + 15, 0, 0, 98, 62, 98, 63);
        Gui.drawRect(context.getMatrices(), this.x + 103, this.y + 15, this.x + 108, this.y + 77, getColor().getRGB());

        //Render R, G, B, A sliders
        r.render(context, 110, 15);
        g.render(context, 110, 15 + 10 + 2);
        b.render(context, 110, 15 + 10 + 10 + 4);
        a.render(context, 110, 15 + 10 + 10 + 10 + 6);

        if (r.hasUpdated() || g.hasUpdated() || b.hasUpdated() || a.hasUpdated()) {
            updateNode();
        }

        //Render buttons
        applyButton.render(context, 112, 65);
        resetButton.render(context, 155, 65);

        //Color spectrum picker
        if (mouseX >= this.x && mouseX <= this.x + 100 && mouseY >= this.y + 10 && mouseY <= this.y + 10 + this.height && Mouse.isButtonDown(0)) {
            for (SliderComponent component : SliderComponent.list) {
                if (component.window.isToggled() && component.clicked) {
                    return;
                }
            }

            try {
                int imageX = (mouseX - this.x) * 6;
                int imageY = (int)((mouseY - (this.y + 10)) * 5.5);
                Color color = new Color(image.getRGB(imageX, imageY));

                r.value = color.getRed();
                g.value = color.getGreen();
                b.value = color.getBlue();
                updateNode();
            } catch (Exception ignored) {

            }
        }
    }

    public void setColors(int hex) {
        Color color = new Color(hex, true);
        r.value = color.getRed();
        g.value = color.getGreen();
        b.value = color.getBlue();
        a.value = color.getAlpha();
    }

    public Color getColor() {
        return new Color((int)r.value, (int)g.value, (int)b.value, (int)a.value);
    }

    public void updateNode() {
        if (currentNode != null) {
            currentNode.setValue("" + getColor().getRGB());
        }
    }
}
