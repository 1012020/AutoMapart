package me.bebeli555.automapart.gui.windows.windows.other;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.gui.windows.TitledWindow;
import me.bebeli555.automapart.gui.windows.components.ButtonComponent;
import me.bebeli555.automapart.gui.windows.components.SliderComponent;
import me.bebeli555.automapart.gui.windows.windows.tools.TextureEditorTool;
import me.bebeli555.automapart.utils.input.Mouse;
import me.bebeli555.automapart.utils.objects.Timer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ImageEditorWindow extends TitledWindow {
    public static ImageEditorWindow INSTANCE;

    public SliderComponent hueSlider = new SliderComponent(this, "Hue", 180, 0, 360, 1, 80, 12);
    public SliderComponent brightnessSlider = new SliderComponent(this, "Brightness", 180, 0, 360, 1, 80,12);
    public SliderComponent saturationSlider = new SliderComponent(this, "Saturation", 180, 0, 360, 1, 80, 12);
    public SliderComponent alphaSlider = new SliderComponent(this, "Alpha", 0, 255, 80, 12);

    public ButtonComponent saveButton = new ButtonComponent(this, "Save image", 10, false);

    public ButtonComponent pencilColorButton = new ButtonComponent(this, "Pencil color", 10, false);

    public NativeImageBackedTexture texture;
    public NativeImage image, editedImage;
    public Identifier editedImageIdentifier;
    public Identifier imagePath;
    public List<PencilPoint> pencilPoints = new ArrayList<>();
    public long enabledTime;
    public int pencilColor = -1;
    public Point hoverPoint;

    public Timer timer = new Timer();

    public ImageEditorWindow() {
        super("ImageEditor", 225, 300);
        INSTANCE = this;

        pencilColorButton.addClickListener(() -> {
            ColorPickerWindow.INSTANCE.setColors(pencilColor);
            ColorPickerWindow.INSTANCE.enable(this.x, this.y);
        });

        ColorPickerWindow.INSTANCE.resetButton.addClickListener(() -> {
            if (this.toggled) {
                ColorPickerWindow.INSTANCE.setColors(-1);
            }
        });

        ColorPickerWindow.INSTANCE.applyButton.addClickListener(() -> {
            if (this.toggled) {
                pencilColor = ColorPickerWindow.INSTANCE.getColor().getRGB();
                ColorPickerWindow.INSTANCE.disable();
            }
        });
    }

    @Override
    public void onOutsideClick() {
        disable();

        if (TextureEditorTool.INSTANCE.toggled) {
            if (!TextureEditorTool.INSTANCE.texturePicker.onlyShowSelected) {
                TextureEditorTool.INSTANCE.texturePicker.selected.clear();
            } else {
                TextureEditorTool.INSTANCE.onEnabled();
            }

            TextureEditorTool.INSTANCE.texturePicker.enable(this.x, this.y);
        }
    }

    @Override
    public void onEnabled() {
        hueSlider.value = hueSlider.defaultValue;
        brightnessSlider.value = brightnessSlider.defaultValue;
        saturationSlider.value = saturationSlider.defaultValue;
        alphaSlider.value = alphaSlider.defaultValue;

        pencilPoints.clear();
        pencilColor = -1;

        hoverPoint = null;
        texture = null;

        enabledTime = System.currentTimeMillis();
    }

    @Override
    public void onDraw(DrawContext context, int mouseX, int mouseY, int lastMouseX, int lastMouseY) {
        this.title = "ImageEditor: " + imagePath.getPath();

        //Draw with pencil
        int pencilX = mouseX - this.x - 12;
        int pencilY = mouseY - this.y - 90;

        hoverPoint = null;
        if (pencilX >= 0 && pencilX <= 200 && pencilY >= 0 && pencilY <= 200 && texture != null) {
            Point point = new Point((int)(pencilX / (200f / texture.getImage().getWidth())), (int)(pencilY / (200f / texture.getImage().getHeight())));
            if (Mouse.isButtonDown(0) && Math.abs(System.currentTimeMillis() - enabledTime) > 350) {
                if (pencilPoints.stream().noneMatch(p -> p.point.equals(point) && p.color == pencilColor)) {
                    Color c = new Color(pencilColor, true);
                    pencilPoints.add(new PencilPoint(point, new Color(c.getBlue(), c.getGreen(), c.getRed(), c.getAlpha()).getRGB()));
                }
            }

            hoverPoint = point;
        }

        //Edit image
        if (timer.hasPassed(35)) {
            timer.reset();

            try {
                NativeImage nativeImage = NativeImage.read(image.getBytes());

                //Convert each pixel's RGB color to HSB, modify the hue, and convert back to RGB
                for (int y = 0; y < nativeImage.getHeight(); y++) {
                    for (int x = 0; x < nativeImage.getWidth(); x++) {
                        int rgb = nativeImage.getColor(x, y);
                        float[] hsb = Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, null);

                        double hueShift = (hueSlider.value - 180) / 180f;
                        double brightnessShift = (brightnessSlider.value - 180) / 180f;
                        double saturationShift = (saturationSlider.value - 180) / 180f;

                        double newHue = (hsb[0] + hueShift) % 1.0f;
                        double newBrightness = Math.max(0, Math.min(1, hsb[2] + brightnessShift));
                        double newSaturation = Math.max(0, Math.min(1, hsb[1] + saturationShift));
                        int newRGB = Color.HSBtoRGB((float)newHue, (float)newSaturation, (float)newBrightness);

                        Color c = new Color(newRGB, true);
                        c = new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, new Color(rgb, true).getAlpha() - Math.abs(255 - (int)alphaSlider.value)));

                        nativeImage.setColor(x, y, c.getRGB());
                    }
                }

                //Draw pencil points
                for (PencilPoint point : pencilPoints) {
                    try {
                        nativeImage.setColor(point.point.x, point.point.y, point.color);
                    } catch (IllegalArgumentException ignored) {}
                }

                NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
                this.editedImageIdentifier = mc.getTextureManager().registerDynamicTexture("editedtexture" + Mod.MOD_ID, texture);
                texture.upload();

                this.texture = texture;
                if (editedImage != image) {
                    editedImage.close();
                }

                editedImage = nativeImage;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int drawX = this.x + 12;
        int drawY = this.y + 90;
        int size = 200;

        Gui.drawRect(context.getMatrices(), drawX, drawY, drawX + size, drawY + size, new Color(0, 0, 0, 125).getRGB());
        context.drawTexture(editedImageIdentifier, drawX, drawY, 0, 0, size, size, size, size);

        hueSlider.render(context, 12, 25);
        brightnessSlider.render(context, 12, 25 + 15);
        saturationSlider.render(context, 12, 25 + 15 * 2);
        alphaSlider.render(context, 12, 25 + 15 * 3);

        saveButton.render(context, 165, 25);

        pencilColorButton.render(context, 101 + 47, 70);
        Gui.drawRect(context.getMatrices(), this.x + 150 + 47, this.y + 54 + 15, this.x + 165 + 47, this.y + 66 + 15, pencilColor);

        //Render hover point
        if (hoverPoint != null) {
            float x = this.x + 12 + (hoverPoint.x * (200f / texture.getImage().getWidth()));
            float y = this.y + 90 + (hoverPoint.y * (200f / texture.getImage().getHeight()));

            Gui.drawRect(context.getMatrices(), x, y, x + 200f / texture.getImage().getWidth(), y + 200f / texture.getImage().getHeight(), new Color(0, 0, 0, 80).getRGB());
        }
    }

    public void setImage(NativeImage image, Identifier imagePath) {
        this.image = image;
        this.imagePath = imagePath;
        this.editedImage = image;
    }

    public record PencilPoint(Point point, int color) {}
}
