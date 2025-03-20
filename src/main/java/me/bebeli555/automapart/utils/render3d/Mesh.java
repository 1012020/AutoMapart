/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package me.bebeli555.automapart.utils.render3d;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.bebeli555.automapart.utils.Utils;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL32C.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Mesh extends Utils {
    public enum Attrib {
        Float(1, 4, false),
        Vec2(2, 4, false),
        Vec3(3, 4, false),
        Color(4, 1, true);

        public final int count, size;
        public final boolean normalized;

        Attrib(int count, int componentSize, boolean normalized) {
            this.count = count;
            this.size = count * componentSize;
            this.normalized = normalized;
        }

        public int getType() {
            return this == Color ? GL_UNSIGNED_BYTE : GL_FLOAT;
        }
    }

    public static boolean depth = false;
    public double alpha = 1;

    private final DrawMode drawMode;
    private final int primitiveVerticesSize;

    private final int vao, vbo, ibo;

    public float lineWidth;
    private ByteBuffer vertices;
    private long verticesPointerStart, verticesPointer;

    private ByteBuffer indices;
    private long indicesPointer;

    private int vertexI, indicesCount;

    private boolean building;
    private double cameraX, cameraZ;
    private boolean beganRendering;

    public static int CURRENT_IBO;
    private static int prevIbo;

    public Mesh(DrawMode drawMode, Attrib... attributes) {
        int stride = 0;
        for (Attrib attribute : attributes) stride += attribute.size;

        this.drawMode = drawMode;
        this.primitiveVerticesSize = stride * drawMode.indicesCount;

        vertices = BufferUtils.createByteBuffer(primitiveVerticesSize * 256 * 4);
        verticesPointerStart = memAddress0(vertices);

        indices = BufferUtils.createByteBuffer(drawMode.indicesCount * 512 * 4);
        indicesPointer = memAddress0(indices);

        vao = GlStateManager._glGenVertexArrays();
        GlStateManager._glBindVertexArray(vao);

        vbo = GlStateManager._glGenBuffers();
        GlStateManager._glBindBuffer(GL_ARRAY_BUFFER, vbo);

        ibo = GlStateManager._glGenBuffers();
        GlStateManager._glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);

        int offset = 0;
        for (int i = 0; i < attributes.length; i++) {
            Attrib attrib = attributes[i];

            GlStateManager._enableVertexAttribArray(i);
            GlStateManager._vertexAttribPointer(i, attrib.count, attrib.getType(), attrib.normalized, stride, offset);

            offset += attrib.size;
        }

        GlStateManager._glBindVertexArray(0);
        GlStateManager._glBindBuffer(GL_ARRAY_BUFFER, 0);
        GlStateManager._glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void destroy() {
        GlStateManager._glDeleteBuffers(ibo);
        GlStateManager._glDeleteBuffers(vbo);
        GlStateManager._glDeleteVertexArrays(vao);
    }

    public void begin() {
        if (building) throw new IllegalStateException("Cant destroy mesh while still building");

        verticesPointer = verticesPointerStart;
        vertexI = 0;
        indicesCount = 0;
        lineWidth = 1;

        building = true;

        Vec3d camera = mc.gameRenderer.getCamera().getPos();
        cameraX = camera.x;
        cameraZ = camera.z;
    }

    public Mesh vec3(Vec3d vec3d) {
        return vec3(vec3d.x, vec3d.y, vec3d.z);
    }

    public Mesh vec3(double x, double y, double z) {
        long p = verticesPointer;

        memPutFloat(p, (float) (x - cameraX));
        memPutFloat(p + 4, (float) y);
        memPutFloat(p + 8, (float) (z - cameraZ));

        verticesPointer += 12;
        return this;
    }

    public Mesh vec2(double x, double y) {
        long p = verticesPointer;

        memPutFloat(p, (float) x);
        memPutFloat(p + 4, (float) y);

        verticesPointer += 8;
        return this;
    }

    public Mesh color(Color c) {
        long p = verticesPointer;

        memPutByte(p, (byte) c.getRed());
        memPutByte(p + 1, (byte) c.getGreen());
        memPutByte(p + 2, (byte) c.getBlue());
        memPutByte(p + 3, (byte) (c.getAlpha() * (float) alpha));

        verticesPointer += 4;
        return this;
    }

    public int next() {
        return vertexI++;
    }

    public void line(int i1, int i2) {
        long p = indicesPointer + indicesCount * 4L;

        memPutInt(p, i1);
        memPutInt(p + 4, i2);

        indicesCount += 2;
        growIfNeeded();
    }

    public void quad(Color color, Vec3d vec1, Vec3d vec2, Vec3d vec3, Vec3d vec4) {
        this.quad(
                this.vec3(vec1).color(color).next(),
                this.vec3(vec2).color(color).next(),
                this.vec3(vec3).color(color).next(),
                this.vec3(vec4).color(color).next()
        );
    }

    public void quad(int i1, int i2, int i3, int i4) {
        long p = indicesPointer + indicesCount * 4L;

        memPutInt(p, i1);
        memPutInt(p + 4, i2);
        memPutInt(p + 8, i3);

        memPutInt(p + 12, i3);
        memPutInt(p + 16, i4);
        memPutInt(p + 20, i1);

        indicesCount += 6;
        growIfNeeded();
    }

    public void triangle(int i1, int i2, int i3) {
        long p = indicesPointer + indicesCount * 4L;

        memPutInt(p, i1);
        memPutInt(p + 4, i2);
        memPutInt(p + 8, i3);

        indicesCount += 3;
        growIfNeeded();
    }

    public void growIfNeeded() {
        // Vertices
        if ((vertexI + 1) * primitiveVerticesSize >= vertices.capacity()) {
            int offset = getVerticesOffset();

            int newSize = vertices.capacity() * 2;
            if (newSize % primitiveVerticesSize != 0) newSize += newSize % primitiveVerticesSize;

            ByteBuffer newVertices = BufferUtils.createByteBuffer(newSize);
            memCopy(memAddress0(vertices), memAddress0(newVertices), offset);

            vertices = newVertices;
            verticesPointerStart = memAddress0(vertices);
            verticesPointer = verticesPointerStart + offset;
        }

        // Indices
        if (indicesCount * 4 >= indices.capacity()) {
            int newSize = indices.capacity() * 2;
            if (newSize % drawMode.indicesCount != 0) newSize += newSize % (drawMode.indicesCount * 4);

            ByteBuffer newIndices = BufferUtils.createByteBuffer(newSize);
            memCopy(memAddress0(indices), memAddress0(newIndices), indicesCount * 4L);

            indices = newIndices;
            indicesPointer = memAddress0(indices);
        }
    }

    public void end() {
        if (!building) throw new IllegalStateException("Mesh.end() called while not building.");

        if (indicesCount > 0) {
            GlStateManager._glBindBuffer(GL_ARRAY_BUFFER, vbo);
            GlStateManager._glBufferData(GL_ARRAY_BUFFER, vertices.limit(getVerticesOffset()), GL_DYNAMIC_DRAW);
            GlStateManager._glBindBuffer(GL_ARRAY_BUFFER, 0);

            bindIndexBuffer(ibo);
            GlStateManager._glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.limit(indicesCount * 4), GL_DYNAMIC_DRAW);
            bindIndexBuffer(0);
        }

        building = false;
    }

    private void bindIndexBuffer(int ibo) {
        if (ibo != 0) prevIbo = CURRENT_IBO;
        GlStateManager._glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo != 0 ? ibo : prevIbo);
    }

    public void beginRender(MatrixStack matrices) {
        if (depth) GlStateManager._enableDepthTest();
        else GlStateManager._disableDepthTest();
        GlStateManager._enableBlend();
        GlStateManager._disableCull();
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(lineWidth);

        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();

        if (matrices != null) matrixStack.multiplyPositionMatrix(matrices.peek().getPositionMatrix());

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        matrixStack.translate(0, -cameraPos.y, 0);

        beganRendering = true;
    }

    public void render(MatrixStack matrices) {
        if (building) end();

        if (indicesCount > 0) {
            // Setup opengl state and matrix stack
            boolean wasBeganRendering = beganRendering;
            if (!wasBeganRendering) beginRender(matrices);

            // Render
            beforeRender();

            Shader.BOUND.setDefaults();

            GlStateManager._glBindVertexArray(vao);
            GlStateManager._drawElements(drawMode.getGL(), indicesCount, GL_UNSIGNED_INT, 0);

            // Cleanup opengl state and matrix stack
            BufferRenderer.resetCurrentVertexBuffer();
            GlStateManager._glBindVertexArray(0);

            if (!wasBeganRendering) endRender();
        }
    }

    public void endRender() {
        RenderSystem.getModelViewStack().pop();

        glDisable(GL_LINE_SMOOTH);

        beganRendering = false;
    }

    public boolean isBuilding() {
        return building;
    }

    protected void beforeRender() {}

    private int getVerticesOffset() {
        return (int) (verticesPointer - verticesPointerStart);
    }
}
