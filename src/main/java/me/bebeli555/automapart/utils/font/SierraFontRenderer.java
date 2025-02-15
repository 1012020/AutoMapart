package me.bebeli555.automapart.utils.font;

import com.mojang.blaze3d.systems.RenderSystem;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.hud.components.DevPanelComponent;
import me.bebeli555.automapart.utils.Renderer2DIn3D;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Custom font renderer that generates images then blits them to the screen
 * The 3d rendering process here is quite complicated and shitty
 */
public class SierraFontRenderer extends Mod {
    public static List<String> builtInFonts = new ArrayList<>();

    public NavigableMap<Integer, HashImage> hash = new TreeMap<>();
    public NavigableMap<Integer, Integer> widthHash = new TreeMap<>();
    public int lastMcScale;

    public void drawCenteredString(MatrixStack stack, String s, float x, float width, float y, int color) {
        drawString(stack, s, x + width / 2 - getWidth(stack, s) / 2f, y, color);
    }

    public void drawString(MatrixStack stack, String s, float x, float y, int color) {
        stack.push();
        if (is3d()) {
            double diff = get3dDistanceSizeDiff(stack, true);
            x /= diff;
            y /= diff;

            float realSize = getCurrentFontSize(stack);
            if (realSize > 100) {
                diff = realSize / 100f;
                x /= diff;
                y /= diff;
            }
        }

        render(stack, s, x, y, color);
        stack.pop();
    }

    public int getWidth(MatrixStack stack, String s) {
        String scale = "" + get3dScale(stack);
        scale = scale.substring(0, (Math.min(scale.length(), 7)));

        int hashCode = (s + getCurrentFont(stack).getSize() + scale).hashCode();

        Integer width = widthHash.get(hashCode);
        if (width == null) {
            String text = "";
            for (FontTextColor.TextColor textColor : new FontTextColor(s, -1).list) {
                text += textColor.text();
            }

            double width2 = generateImage(stack, text, -1).getWidth() / (is3d() ? 1 : getScale(stack)) / (is3d() ? 1 : getMcScale()) / (is3d() ? get3dDistanceSizeDiff(stack, false) : 1);
            if (is3d()) {
                float realSize = getCurrentFontSize(stack);
                if (realSize > 100) {
                    width2 *= (realSize / 100f);
                }
            }

            width = Math.round((float)width2);
            widthHash.put(hashCode, width);

            if (widthHash.size() > 50000) {
                clearHashes();
            }
        }

        return width;
    }

    public int getHeight(MatrixStack stack) {
        return (int)(getFontSize());
    }

    private void render(MatrixStack stack, String s, double x, double y, int color) {
        if (s == null || s.isEmpty()) {
            return;
        }

        DevPanelComponent.put("FontStringHashes" + (is3d() ? "3D" : "2D"), hash.size());
        DevPanelComponent.put("FontWidthHashes" + (is3d() ? "3D" : "2D"), widthHash.size());

        int mcScale = getMcScale();
        if (mcScale != lastMcScale) {
            clearHashes();
        }

        lastMcScale = mcScale;
        int hashCode = (s + getCurrentFont(stack).getSize() + color).hashCode();

        HashImage hashImage = hash.get(hashCode);
        Identifier identifier;
        int width, height;

        if (hashImage != null) {
            identifier = hashImage.identifier;
            width = hashImage.width;
            height = hashImage.height;
        } else {
            List<BufferedImage> images = new ArrayList<>();
            for (FontTextColor.TextColor textColor : new FontTextColor(s, color).list) {
                BufferedImage image = generateImage(stack, textColor.text(), textColor.color());
                images.add(image);
            }

            int totalWidth = 0;
            for (BufferedImage image : images) {
                totalWidth += image.getWidth();
            }

            if (images.isEmpty()) {
                return;
            }

            BufferedImage mainImage = new BufferedImage(totalWidth, images.get(0).getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = mainImage.createGraphics();

            int currentX = 0;
            for (BufferedImage image : images) {
                graphics.drawImage(image, currentX, 0, null);
                currentX += image.getWidth();
            }

            graphics.dispose();

            NativeImage image = bufferedToNative(mainImage);
            NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
            identifier = mc.getTextureManager().registerDynamicTexture("fontimage" + hashCode, texture);
            width = image.getWidth();
            height = image.getHeight();

            hash.put(hashCode, new HashImage(identifier, width, height));
        }

        renderImage(stack, x, y, width, height, identifier);
        if (hash.size() > 30000) {
            clearHashes();
        }
    }

    private void renderImage(MatrixStack stack, double x, double y, double width, double height, Identifier identifier) {
        double scale = getScale(stack) * getMcScale();

        RenderSystem.enableBlend();

        int textureId = mc.getTextureManager().getTexture(identifier).getGlId();
        RenderSystem.setShaderTexture(0, textureId);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        if (is3d()) {
            blit(stack, (float)x, (float)y, (float)width, (float)height);
        } else {
            blit(null, (float)(x * scale), (float)((y + getYAdd() - (getFontSize() - 9)) * scale), (float)width, (float)height);
        }

        RenderSystem.disableBlend();
    }

    private BufferedImage generateImage(MatrixStack stack, String text, int color) {
        Font font = getCurrentFont(stack);

        BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tempGraphics = tempImage.createGraphics();
        tempGraphics.setFont(font);
        FontMetrics fontMetrics = tempGraphics.getFontMetrics();

        int width = fontMetrics.stringWidth(text) + 1 + (text.length() * getGap());
        if (getFontType().equals("Italic")) {
            width += 3;
        }

        int height = fontMetrics.getHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setFont(font);

        g2d.setBackground(new Color(0, 0, 0, 0));
        g2d.clearRect(0, 0, image.getWidth(), image.getHeight());

        //Draw shadow
        if (isShadow()) {
            g2d.setColor(new Color(getShadowColor(), true));
            int size = getShadowOffset();
            drawTextWithCharacterGap(g2d, text,  size * (font.getSize() / 50f), fontMetrics.getAscent() + size * (font.getSize() / 50f), getGap());
        }

        Color temp = new Color(color, true);
        g2d.setColor(new Color(temp.getBlue(), temp.getGreen(), temp.getRed(), temp.getAlpha()));
        drawTextWithCharacterGap(g2d, text, 0, fontMetrics.getAscent(), getGap());
        g2d.dispose();

        return image;
    }

    private void drawTextWithCharacterGap(Graphics2D g, String text, float x, float y, int characterGap) {
        char[] characters = text.toCharArray();
        float xPos = x;

        for (char c : characters) {
            int charWidth = g.getFontMetrics().charWidth(c);
            g.drawString(String.valueOf(c), xPos, y);

            xPos += characterGap;
            xPos += charWidth;
        }
    }

    private NativeImage bufferedToNative(BufferedImage bufferedImage) {
        NativeImage nativeImage = new NativeImage(bufferedImage.getWidth(), bufferedImage.getHeight(), false);

        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                nativeImage.setColor(x, y, bufferedImage.getRGB(x, y));
            }
        }

        return nativeImage;
    }

    private float getCurrentFontSize(MatrixStack stack) {
        float size = (int)((getScale(stack) * getFontSize()) * getMcScale());

        if (is3d()) {
            size = 28 / get3dScale(stack);
            size *= get3dDistanceSizeDiff(stack, false);
        }

        return size;
    }

    public static float get3dScale(MatrixStack stack) {
        return (0.875f / ((stack.peek().getPositionMatrix().getScale(new Vector3f()).x / Renderer2DIn3D.lastPerspectiveScale) * 100));
    }

    private Font getCurrentFont(MatrixStack stack) {
        String setting = getFontType();
        int type = setting.equals("Italic") ? 2 : (setting.equals("Plain") ? 0 : 1);

        int usedSize = Math.min(100, Math.max(1, Math.round(getCurrentFontSize(stack))));
        DevPanelComponent.put("LastFontSize" + (is3d() ? "3D" : "2D"), usedSize);

        return new Font(getFontName(), type, usedSize);
    }

    public static double get3dDistanceSizeDiff(MatrixStack stack, boolean reverse) {
        int size = Math.round(28 / get3dScale(stack));
        double dist = Renderer2DIn3D.activePos.distanceTo(mc.gameRenderer.getCamera().getPos());
        if (dist < Renderer2DIn3D.minDist) {
            if (reverse) {
                return size / (double)Math.round(size * (11 - dist));
            } else {
                return (double)Math.round(size * (11 - dist))  / size;
            }
        }

        return 1;
    }

    public static double getScale(MatrixStack stack) {
        if (stack == null) {
            return 1;
        }

        Vector3f vec = stack.peek().getPositionMatrix().getScale(new Vector3f());
        DevPanelComponent.put("LastPoseScale", vec.x);

        return vec.x;
    }

    public static int getMcScale() {
        return (int)mc.getWindow().getScaleFactor();
    }

    public static void registerFonts() {
        try {
            String[] fonts = {"firasans", "helvetica", "indieflower", "interregular", "mochiy", "moonrocks", "orbitron", "oswald", "tcm", "minecraft"};

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            for (String name : fonts) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, SierraFontRenderer.class.getResourceAsStream("/fonts/" + name + ".ttf"));
                ge.registerFont(font);

                builtInFonts.add(font.getFamily());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void blit(MatrixStack stack, float x, float y, float width, float height) {
        if (width * height > height * width) {
            height = width * (height / width);
        } else {
            width = height * (width / height);
        }

        //3d rendering
        if (stack != null) {
            Matrix4f matrix = new Matrix4f(stack.peek().getPositionMatrix());
            matrix.scale(get3dScale(stack));
            matrix.scale(2.5f);
            matrix.scale((float)get3dDistanceSizeDiff(stack, true));

            float realSize = getCurrentFontSize(stack);
            if (realSize > 100) {
                matrix.scale(realSize / 100f);
            }

            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
            bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            bufferbuilder.vertex(matrix, x, y + height, 0).texture(0, 1).next();
            bufferbuilder.vertex(matrix, x + width, y + height, 0).texture(1, 1).next();
            bufferbuilder.vertex(matrix, x + width, y, 0).texture(1, 0).next();
            bufferbuilder.vertex(matrix, x, y, 0).texture(0, 0).next();
            BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());
            return;
        }

        float scale = (float)1 / getMcScale();
        stack = new MatrixStack();
        stack.scale(scale, scale, scale);
        Matrix4f matrix = stack.peek().getPositionMatrix();

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferbuilder.vertex(matrix, (int)x, (int)(y + height), 0).texture(0, 1).next();
        bufferbuilder.vertex(matrix, (int)(x + width), (int)(y + height), 0).texture(1, 1).next();
        bufferbuilder.vertex(matrix, (int)(x + width), (int)y, 0).texture(1, 0).next();
        bufferbuilder.vertex(matrix, (int)x, (int)y, 0).texture(0, 0).next();
        BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());
    }

    public void clearHashes() {
        for (HashImage hashImage : hash.values()) {
            mc.getTextureManager().getTexture(hashImage.identifier).close();
        }

        hash.clear();
        widthHash.clear();
    }

    public double getFontSize() {return 0;}
    public int getYAdd() {return 0;}
    public int getGap() {return 0;}
    public String getFontName() {return null;}
    public String getFontType() {return null;}
    public boolean isShadow() {return false;}
    public int getShadowColor() {return 0;}
    public int getShadowOffset() {return 0;}
    public boolean is3d() {return false;}

    public static class HashImage {
        public Identifier identifier;
        public int width, height;

        public HashImage(Identifier Identifier, int width, int height) {
            this.identifier = Identifier;
            this.width = width;
            this.height = height;
        }
    }
}
