package me.bebeli555.automapart.hud.components;

import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.gui.windows.windows.pickers.pickers.EntityTypePicker;
import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.utils.globalsettings.GlobalBorderSettings;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.SettingList;
import me.bebeli555.automapart.settings.SettingValue;
import me.bebeli555.automapart.utils.BlockUtils;
import me.bebeli555.automapart.utils.EntityUtils;
import me.bebeli555.automapart.utils.RenderUtils2D;
import me.bebeli555.automapart.utils.objects.Timer;
import me.bebeli555.automapart.utils.font.SierraFontRenderer;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.dimension.DimensionTypes;

import java.awt.*;
import java.util.*;
import java.util.List;

public class MiniMapComponent extends HudComponent {
    public Timer hashCleanerTimer = new Timer();
    public List<BlockPos> blocks = new ArrayList<>();
    public Vec3d lastPos = new Vec3d(0, 0, 0);
    public HashMap<BlockPos, Integer> hash = new HashMap<>();
    public HashMap<BlockPos, Integer> heightMap = new HashMap<>();
    public Identifier identifier;
    public NativeImageBackedTexture texture;
    public int lastCheck;
    public boolean threadFinished = true;

    public static Setting minimap = new Setting(Mode.BOOLEAN, "Minimap", false, "Shows minimap");
        public static Setting size = new Setting(minimap, Mode.INTEGER, "Size", new SettingValue(190, 10, 1500, 5), "How many blocks around the player is the size");
        public static Setting scaleSetting = new Setting(minimap, Mode.DOUBLE, "Scale", new SettingValue(3, 0.3, 10, 0.025), "The rendered scale of the minimap");
        public static Setting updateDelay = new Setting(minimap, Mode.INTEGER, "UpdateDelay", new SettingValue(25, 1, 100, 1), "How often to sleep the thread every thousand blocks", "So if this value is 10 every 10k blocks looped, it will sleep 1ms", "Because if you don't wait the thread every now and then, ur computer will blow", "Or ur game crash because the thread is overloaded. So if you want it to load the updates faster", "Set this value higher, basically it starts affecting when you change the size");
        public static Setting yScaling = new Setting(minimap, Mode.LABEL, "YScaling", true, "How much to change the color depending on the Y level");
            public static Setting yScalingScale = new Setting(yScaling, Mode.DOUBLE, "YScale", new SettingValue(10.5, 0, 25, 0.5), "How much to change the color depending on the Y level");
            public static Setting yScalingMaxChange = new Setting(yScaling, Mode.INTEGER, "MaxChange", new SettingValue(50, 1, 255, 1), "Maximum amount to change per position");
            public static Setting yScalingRevert = new Setting(yScaling, Mode.BOOLEAN, "Revert", false, "Changes if it adds or removes from RGB");
        public static SettingList border = GlobalBorderSettings.get(minimap, true, false, -8737798, 1);
        public static Setting entities = new Setting(minimap, Mode.BOOLEAN, "Entities", true, "Entity rendering settings");
            public static Setting entitiesSize = new Setting(entities, Mode.DOUBLE, "Size", new SettingValue(0.8, 0.3, 3, 0.1), "Size of the rendered circles");
            public static Setting entitiesMaxYDiff = new Setting(entities, Mode.INTEGER, "MaxYDiff", new SettingValue(35, 1, 255, 1), "Renders entities only if the difference between you and the entity", "is less than this value, doesnt apply for players");
            public static Setting entityPicker = new Setting(entities, Mode.PICKER, "EntityTypePicker", "Passive$Neutral$Hostile$Players", "Select the entity types you want to render");
        public static Setting brightness = new Setting(minimap, Mode.INTEGER, "Brightness", new SettingValue(255, 0, 255, 1), "Minimap brightness");
    
    public MiniMapComponent() {
        super(HudCorner.TOP_LEFT, minimap);
        this.defaultX = 53;
        this.defaultY = 62;
    }

    @Override
    public void onRender(DrawContext context, float partialTicks) {
        MatrixStack stack = context.getMatrices();

        //Clean hashmaps
        if (hashCleanerTimer.hasPassed(10000)) {
            hashCleanerTimer.reset();
            new Thread(() -> {
                List<Map<BlockPos, Integer>> maps = Arrays.asList(hash, heightMap);

                for (Map<BlockPos, Integer> map : maps) {
                    try {
                        List<BlockPos> keysToRemove = new ArrayList<>();

                        for (BlockPos pos : map.keySet()) {
                            if (pos != null && BlockUtils.distance(pos, mc.player.getBlockPos()) > Math.max(200, size.asInt()) * 4) {
                                keysToRemove.add(pos);
                            }
                        }

                        for (BlockPos key : keysToRemove) {
                            map.remove(key);
                        }
                    } catch (ConcurrentModificationException ignored) {}
                }
            }).start();
        }

        if (threadFinished) {
            threadFinished = false;
            new Thread(() -> {
                try {
                    blocks = getBlocksAroundPlayer();
                    if (blocks.isEmpty()) {
                        return;
                    }

                    NativeImage image;
                    boolean changed = false;
                    if (texture == null || lastCheck != size.asInt()) {
                        image = new NativeImage(size.asInt() * 2 + 1, size.asInt() * 2 + 1, false);
                        changed = true;
                    } else {
                        image = texture.getImage();
                    }

                    lastCheck = size.asInt();
                    BlockPos first = blocks.get(0);
                    int blocksChecked = 0;

                    for (BlockPos blockPos : blocks) {
                        blocksChecked++;
                        if (blocksChecked > updateDelay.asInt() * 1000) {
                            blocksChecked = 0;
                            sleep(1);
                        }

                        int yScale = Math.abs(blockPos.getY() - getHeight(blockPos.add(1, 0, 0)).getY());
                        yScale += Math.abs(blockPos.getY() - getHeight(blockPos.add(-1, 0, 0)).getY());
                        yScale += Math.abs(blockPos.getY() - getHeight(blockPos.add(0, 0, 1)).getY());
                        yScale += Math.abs(blockPos.getY() - getHeight(blockPos.add(0, 0, -1)).getY());
                        yScale *= yScalingScale.asDouble();

                        if (Math.abs(yScale) > yScalingMaxChange.asInt()) {
                            if (yScale < 0) {
                                yScale = -yScalingMaxChange.asInt();
                            } else {
                                yScale = yScalingMaxChange.asInt();
                            }
                        }

                        boolean revert = yScalingRevert.bool();
                        int color = mc.player.getWorld().getBlockState(blockPos).getMapColor(mc.player.getWorld(), blockPos).getRenderColor(MapColor.Brightness.NORMAL);
                        if (color != 0) {
                            hash.put(new BlockPos(blockPos.getX(), 0, blockPos.getZ()), color);
                        } else {
                            Integer hashColor = hash.get(new BlockPos(blockPos.getX(), 0, blockPos.getZ()));
                            if (hashColor == null) {
                                hashColor = -16777216;
                            }

                            color = hashColor;
                        }

                        Color c = new Color(color);
                        c = new Color(
                                Math.min(255, Math.max(c.getRed() + (revert ? yScale : -yScale), 0)),
                                Math.min(255, Math.max(c.getGreen() + (revert ? yScale : -yScale), 0)),
                                Math.min(255, Math.max(c.getBlue() + (revert ? yScale : -yScale), 0))
                        );
                        color = c.getRGB();

                        image.setColor(Math.min(image.getWidth() - 1, Math.max(0, blockPos.getX() - first.getX())), Math.min(image.getHeight() - 1, Math.max(0, blockPos.getZ() - first.getZ())), color);
                    }

                    if (changed) {
                        texture = new NativeImageBackedTexture(image);
                        identifier = mc.getTextureManager().registerDynamicTexture("minimapimage", texture);
                    }

                    mc.execute(() -> {
                        lastPos = new Vec3d(first.getX() + size.asInt() + 0.5, 0, first.getZ() + size.asInt() + 0.5);
                        texture.upload();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

                sleep(25);
                threadFinished = true;
            }).start();
        }

        if (blocks.isEmpty() || identifier == null) {
            return;
        }

        double size = 50 * scaleSetting.asDouble();
        double x = getxAdd() + size / 2;
        double y = getyAdd() + size / 2;

        //Render image
        stack.push();
        rotate(stack, x, y, size);

        double scale = SierraFontRenderer.getScale(stack);
        int amount = (int) (8 * scaleSetting.asDouble());

        context.enableScissor(
                Math.round((float)((x - size + amount) * scale)),
                Math.round((float)((y - size + amount) * scale)),
                Math.round((float)((x - amount) * scale)),
                Math.round((float)((y - amount) * scale))
        );

        //Translate to current coords
        float calc = (float)(((double)20 / (MiniMapComponent.size.asInt() * 2 / 20)) / (8 / scaleSetting.asDouble()));
        stack.translate((lastPos.x - mc.player.getPos().x) * calc, (lastPos.z - mc.player.getPos().z) * calc, 0);

        context.drawTexture(identifier, (int)(x - size), (int)(y - size), 0, 0, (int)size, (int)size, (int)size, (int)size);

        //Translate back
        stack.translate(-((lastPos.x - mc.player.getPos().x) * calc), -((lastPos.z - mc.player.getPos().z) * calc), 0);

        //Render entities
        if (entities.bool()) {
            Gui.drawRect(stack, 0, 0, 0, 0, -1);
            for (Entity entity : EntityUtils.getAll()) {
                int diff = (int)Math.abs(entity.getPos().y - mc.player.getBlockPos().getY());
                int maxDiff = entitiesMaxYDiff.asInt();
                if (maxDiff != 255 && diff > maxDiff) {
                    if (!(entity instanceof PlayerEntity)) {
                        continue;
                    }
                }

                if (!EntityTypePicker.INSTANCE.isValid(entity, entityPicker)) {
                    continue;
                }

                Color entityColor = EntityTypePicker.INSTANCE.getColor(entity, entityPicker, true);
                int amountSize = MiniMapComponent.size.asInt() - 1;
                Vec3d pos = new Vec3d(mc.player.getPos().x - amountSize, 0, mc.player.getPos().z - amountSize);

                double divide = (double) MiniMapComponent.size.asInt() / size * 2;
                double positionX = (entity.getPos().x - pos.x) / divide;
                double positionY = (entity.getPos().z - pos.z) / divide;
                RenderUtils2D.drawCircle(stack, x + positionX - size, y + positionY - size, (float)(entitiesSize.asDouble() * scaleSetting.asDouble() / 3), 10, entityColor.getRGB());
            }
        }

        //Render brightness
        Gui.drawRect(stack, x - size, y - size, x, y, new Color(0, 0, 0, ~brightness.asInt() & 0xFF).getRGB());
        this.renderedPoints.add(new HudPoint(x - size + amount - 3, y - size + amount - 3, x - amount + 3, y - amount + 3));

        context.disableScissor();
        stack.pop();

        //Render border
        GlobalBorderSettings.render(border, stack, x - size + amount, y - size + amount, x - amount, y - amount);

        //Render player indicator
        double lineWidth = 0.6;
        int lineLength = 3;
        Gui.drawRect(stack, x - size / 2 - lineLength, y - size / 2, x - size / 2 + lineLength + lineWidth, y - size / 2 + lineWidth, -1);
        Gui.drawRect(stack, x - size / 2, y - size / 2 - lineLength, x - size / 2 + lineWidth, y - size / 2 + lineLength + lineWidth, -1);
    }

    public void rotate(MatrixStack stack, double x, double y, double size) {
        size /= 2;
        stack.translate(x, y, 0);
        stack.translate(-size, -size, 0);
        stack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(MathHelper.wrapDegrees(mc.player.getYaw())));
        stack.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(180));
        stack.translate(size, size, 0);
        stack.translate(-x, -y, 0);
    }

    /**
     * Gets the blocks around the players in a rectangle
     * Going from the specified y from down until we hit solid
     */
    public List<BlockPos> getBlocksAroundPlayer() {
        List<BlockPos> list = new ArrayList<>();
        int amount = size.asInt();
        for (int x = (int)(mc.player.getPos().x - amount); x < mc.player.getPos().x + amount; x++) {
            for (int z = (int)(mc.player.getPos().z -amount); z < mc.player.getPos().z + amount; z++) {
                list.add(getHeight(new BlockPos(x, 0, z)));
            }
        }

        return list;
    }

    public BlockPos getHeight(BlockPos blockPos) {
        int height = mc.player.getWorld().getChunk(blockPos).sampleHeightmap(Heightmap.Type.WORLD_SURFACE, blockPos.getX(), blockPos.getZ());
        BlockPos pos = new BlockPos(blockPos.getX(), height, blockPos.getZ());

        if (mc.player.getWorld().getDimensionKey() == DimensionTypes.THE_NETHER) {
            pos = pos.add(0, -5, 0);
            for (int i = 0; i < 122; i++) {
                if (BlockUtils.getBlock(pos) != Blocks.AIR) {
                    break;
                }

                pos = pos.add(0, -1, 0);
            }
        }

        if (BlockUtils.getBlock(pos) == Blocks.AIR) {
            pos = pos.add(0, -1, 0);
        }

        if (pos.getY() <= -64) {
            Integer hashPos = heightMap.get(new BlockPos(blockPos.getX(), 0, blockPos.getZ()));
            if (hashPos != null) {
                pos = new BlockPos(blockPos.getX(), hashPos, blockPos.getZ());
            }
        } else {
            heightMap.put(new BlockPos(blockPos.getX(), 0, blockPos.getZ()), pos.getY());
        }

        return pos;
    }
}
