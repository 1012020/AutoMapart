/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package me.bebeli555.automapart.utils.render3d;

import me.bebeli555.automapart.utils.Utils;
import me.bebeli555.automapart.utils.objects.MixinParameters;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class Renderer3D extends Utils {
    public final Mesh lines = new ShaderMesh(Shaders.POS_COLOR, DrawMode.Lines, Mesh.Attrib.Vec3, Mesh.Attrib.Color);
    public final Mesh triangles = new ShaderMesh(Shaders.POS_COLOR, DrawMode.Triangles, Mesh.Attrib.Vec3, Mesh.Attrib.Color);

    /**
     * Clears the buffers and begins a new rendering cycle
     */
    public void begin() {
        lines.begin();
        triangles.begin();
    }

    /**
     * Renders the set elements, must be called after begin and some render code
     */
    public void render() {
        lines.render(MixinParameters.RENDERED3D_MATRICES);
        triangles.render(MixinParameters.RENDERED3D_MATRICES);
    }

    /**
     * Renders a line from point a to point b
     */
    public void line(Vec3d start, Vec3d end, float lineWidth, Color color, boolean render) {
        if (render) begin();

        Vec3d direction = end.subtract(start).normalize();
        Vec3d last = start;

        for (int i = 0; i < 25000; i++) {
            Vec3d outcome = last.add(direction.multiply(0.1));
            if (outcome.distanceTo(end) <= 0.3) {
                outcome = end;
            }

            doLine(last, outcome, lineWidth, color);
            last = outcome;

            if (last == end) {
                break;
            }
        }

        if (render) render();
    }

    public void doLine(Vec3d start, Vec3d end, float lineWidth, Color color) {
        lines.lineWidth = lineWidth;
        lines.line(lines.vec3(start.x, start.y, start.z).color(color).next(), lines.vec3(end.x, end.y, end.z).color(color).next());
    }

    /**
     * Draws a line but doesn't begin and render the buffer yet
     */
    public void line(Vec3d start, Vec3d end, float lineWidth, Color color) {
        line(start, end, lineWidth, color, false);
    }

    /**
     * Transforms blockpos to vec and so it would prevent culling on depth
     */
    public Vec3d toVec(BlockPos pos) {
        double min = 0.002;
        return new Vec3d(pos.getX() + (pos.getX() < 0 ? min : -min), pos.getY() + min, pos.getZ() + (pos.getZ() < 0 ? min : -min));
    }

    /**
     * Draws an outline of lines (4 lines) at the bottom of the given block position
     */
    public void linesAroundTop(BlockPos pos, float lineWidth, Color color) {
        Vec3d vec = toVec(pos);
        boxLines(new Box(vec.x, vec.y + 1, vec.z, vec.x + 1, vec.y + 1, vec.z + 1), lineWidth, color);
    }

    /**
     * Draws a box of lines around the pos
     */
    public void boxLines(BlockPos pos, float lineWidth, Color color) {
        boxLines(new Box(toVec(pos), toVec(pos).add(1, 1, 1)), lineWidth, color);
    }

    /**
     * Draws a filled quad at the top of the pos
     */
    public void fillTop(BlockPos pos, Color color) {
        Vec3d vec = toVec(pos);
        fill(new Box(vec.x, vec.y + 1, vec.z, vec.x + 1, vec.y + 1, vec.z + 1), color);
    }

    /**
     * Fills the block completely
     */
    public void fill(BlockPos pos, Color color) {
        fill(new Box(toVec(pos), toVec(pos).add(1, 1, 1)), color);
    }

    /**
     * Renders lines of the edges on this bounding box
     */
    public void boxLines(Box box, float lineWidth, Color color) {
        begin();

        //Bottom
        line(new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.maxX, box.minY, box.minZ), lineWidth, color);
        line(new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.minX, box.minY, box.maxZ), lineWidth, color);
        line(new Vec3d(box.maxX, box.minY, box.minZ), new Vec3d(box.maxX, box.minY, box.maxZ), lineWidth, color);
        line(new Vec3d(box.minX, box.minY, box.maxZ), new Vec3d(box.maxX, box.minY, box.maxZ), lineWidth, color);

        //Top
        line(new Vec3d(box.minX, box.maxY, box.minZ), new Vec3d(box.maxX, box.maxY, box.minZ), lineWidth, color);
        line(new Vec3d(box.minX, box.maxY, box.minZ), new Vec3d(box.minX, box.maxY, box.maxZ), lineWidth, color);
        line(new Vec3d(box.maxX, box.maxY, box.minZ), new Vec3d(box.maxX, box.maxY, box.maxZ), lineWidth, color);
        line(new Vec3d(box.minX, box.maxY, box.maxZ), new Vec3d(box.maxX, box.maxY, box.maxZ), lineWidth, color);

        //Sides
        line(new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.minX, box.maxY, box.minZ), lineWidth, color);
        line(new Vec3d(box.maxX, box.minY, box.minZ), new Vec3d(box.maxX, box.maxY, box.minZ), lineWidth, color);
        line(new Vec3d(box.minX, box.minY, box.maxZ), new Vec3d(box.minX, box.maxY, box.maxZ), lineWidth, color);
        line(new Vec3d(box.maxX, box.minY, box.maxZ), new Vec3d(box.maxX, box.maxY, box.maxZ), lineWidth, color);

        render();
    }

    /**
     * Renders an entire fill color for this bounding box
     */
    public void fill(Box box, Color color) {
        begin();

        //Top and bottom
        triangles.quad(color, new Vec3d(box.minX, box.maxY, box.minZ), new Vec3d(box.maxX, box.maxY, box.minZ), new Vec3d(box.maxX, box.maxY, box.maxZ), new Vec3d(box.minX, box.maxY, box.maxZ));
        triangles.quad(color, new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.maxX, box.minY, box.minZ), new Vec3d(box.maxX, box.minY, box.maxZ), new Vec3d(box.minX, box.minY, box.maxZ));

        //Sides
        triangles.quad(color, new Vec3d(box.minX, box.maxY, box.minZ), new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.maxX, box.minY, box.minZ), new Vec3d(box.maxX, box.maxY, box.minZ));
        triangles.quad(color, new Vec3d(box.minX, box.maxY, box.minZ), new Vec3d(box.minX, box.minY, box.minZ), new Vec3d(box.minX, box.minY, box.maxZ), new Vec3d(box.minX, box.maxY, box.maxZ));
        triangles.quad(color, new Vec3d(box.maxX, box.maxY, box.maxZ), new Vec3d(box.maxX, box.minY, box.maxZ), new Vec3d(box.minX, box.minY, box.maxZ), new Vec3d(box.minX, box.maxY, box.maxZ));
        triangles.quad(color, new Vec3d(box.maxX, box.maxY, box.maxZ), new Vec3d(box.maxX, box.minY, box.maxZ), new Vec3d(box.maxX, box.minY, box.minZ), new Vec3d(box.maxX, box.maxY, box.minZ));

        render();
    }
}
