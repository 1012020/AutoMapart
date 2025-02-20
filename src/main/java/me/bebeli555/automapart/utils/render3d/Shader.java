 /*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package me.bebeli555.automapart.utils.render3d;

 import com.google.common.collect.ImmutableList;
 import com.mojang.blaze3d.platform.GlStateManager;
 import com.mojang.blaze3d.systems.RenderSystem;
 import it.unimi.dsi.fastutil.objects.Object2IntMap;
 import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
 import me.bebeli555.automapart.Mod;
 import me.bebeli555.automapart.utils.Utils;
 import net.minecraft.util.Identifier;
 import org.apache.commons.io.IOUtils;
 import org.joml.Matrix4f;
 import org.lwjgl.BufferUtils;

 import java.awt.*;
 import java.io.IOException;
 import java.nio.FloatBuffer;
 import java.nio.charset.StandardCharsets;

 import static org.lwjgl.opengl.GL32C.*;

public class Shader extends Utils {
    public static Shader BOUND;
    private static final FloatBuffer MAT = BufferUtils.createFloatBuffer(4 * 4);

    private final int id;
    private final Object2IntMap<String> uniformLocations = new Object2IntOpenHashMap<>();

    public Shader(String vertPath, String fragPath) {
        int vert = GlStateManager.glCreateShader(GL_VERTEX_SHADER);
        GlStateManager.glShaderSource(vert, ImmutableList.of(read(vertPath)));

        GlStateManager.glCompileShader(vert);
        if (GlStateManager.glGetShaderi(vert, GL_COMPILE_STATUS) == GL_FALSE) {
            String error = GlStateManager.glGetShaderInfoLog(vert, 512);
            System.err.println("Failed to load vert shader: "  + error);
        }

        int frag = GlStateManager.glCreateShader(GL_FRAGMENT_SHADER);
        GlStateManager.glShaderSource(frag, ImmutableList.of(read(fragPath)));

        GlStateManager.glCompileShader(frag);
        if (GlStateManager.glGetShaderi(frag, GL_COMPILE_STATUS) == GL_FALSE) {
            String error = GlStateManager.glGetShaderInfoLog(vert, 512);
            System.err.println("Failed to load frag shader: "  + error);
        }

        id = GlStateManager.glCreateProgram();

        GlStateManager.glAttachShader(id, vert);
        GlStateManager.glAttachShader(id, frag);
        GlStateManager.glLinkProgram(id);

        if (GlStateManager.glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
            String error = GlStateManager.glGetProgramInfoLog(id, 512);
            System.err.println("Failed to load program: "  + error);
        }

        GlStateManager.glDeleteShader(vert);
        GlStateManager.glDeleteShader(frag);
    }

    private String read(String path) {
        try {
            return IOUtils.toString(mc.getResourceManager().getResource(new Identifier(Mod.MOD_ID, "shaders/" + path)).get().getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void bind() {
        GlStateManager._glUseProgram(id);
        BOUND = this;
    }

    private int getLocation(String name) {
        if (uniformLocations.containsKey(name)) return uniformLocations.getInt(name);

        int location = GlStateManager._glGetUniformLocation(id, name);
        uniformLocations.put(name, location);
        return location;
    }

    public void set(String name, boolean v) {
        GlStateManager._glUniform1i(getLocation(name), v ? GL_TRUE : GL_FALSE);
    }

    public void set(String name, int v) {
        GlStateManager._glUniform1i(getLocation(name), v);
    }

    public void set(String name, double v) {
        glUniform1f(getLocation(name), (float) v);
    }

    public void set(String name, double v1, double v2) {
        glUniform2f(getLocation(name), (float) v1, (float) v2);
    }

    public void set(String name, Color color) {
        glUniform4f(getLocation(name), (float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, (float) color.getAlpha() / 255);
    }

    public void set(String name, Matrix4f mat) {
        mat.get(MAT);
        GlStateManager._glUniformMatrix4(getLocation(name), false, MAT);
    }

    public void setDefaults() {
        set("PROJECTION", RenderSystem.getProjectionMatrix());
        set("MODELVIEW", RenderSystem.getModelViewStack().peek().getPositionMatrix());
    }
}
