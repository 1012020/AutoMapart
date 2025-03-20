package me.bebeli555.automapart.mods.world;

import com.google.common.eventbus.Subscribe;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.render.Render3DEvent;
import me.bebeli555.automapart.gui.Group;
import me.bebeli555.automapart.settings.*;
import me.bebeli555.automapart.utils.*;
import me.bebeli555.automapart.utils.globalsettings.GlobalPosRenderSettings;
import me.bebeli555.automapart.utils.objects.Timer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarpetBlock;
import net.minecraft.client.gui.screen.ingame.CartographyTableScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.io.File;
import java.util.*;
import java.util.List;

public class AutoMapart extends Mod {
    // Settings
    public static Setting sIdx = new Setting(Mode.INTEGER, "SIdx", 1, "Schematic index", "Increments when finished");
    public static Setting sX = new Setting(Mode.INTEGER, "X", 0, "Schematic X");
    public static Setting sY = new Setting(Mode.INTEGER, "Y", 0, "Schematic Y");
    public static Setting sZ = new Setting(Mode.INTEGER, "Z", 0, "Schematic Z");
    public static Setting sDir = new Setting(Mode.INTEGER, "Dir", new SettingValue(1, 1, 8, 1), "Direction", "Try different numbers");
    public static Setting debug = new Setting(Mode.BOOLEAN, "ChatDbg", true, "Debug info in chat");
    public static Setting render = new Setting(Mode.BOOLEAN, "Render", false, "Render schematic");
    public static Setting sleepSec = new Setting(Mode.DOUBLE, "MapSleep", new SettingValue(5, 0, 60, 0.1), "Sleep seconds for mapping");
    public static Setting resetCache = new Setting(Mode.BOOLEAN, "ResetCache", true, "Reset block cache on disable");
    public static Setting takeDelay = new Setting(Mode.DOUBLE, "TakeDelay", new SettingValue(0.35, 0, 3, 0.01), "Delay for taking items");
    public static Setting pSpeed = new Setting(Mode.DOUBLE, "PSpeed", new SettingValue(0.1, 0, 1, 0.001), "Place speed delay");
    public static Setting rotWait = new Setting(Mode.DOUBLE, "RotWait", new SettingValue(0.1, 0, 1, 0.001), "Delay after rotating");
    public static Setting pDist = new Setting(Mode.DOUBLE, "PDist", new SettingValue(4, 0, 8, 0.05), "Max place distance");
    public static Setting cacheDelay = new Setting(Mode.DOUBLE, "CacheDelay", new SettingValue(0.5, 0, 15, 0.1), "Cache update delay");
    public static Setting resetAttemptDelay = new Setting(Mode.DOUBLE, "ResetAttempt", new SettingValue(15, 0, 1000, 1), "Reset attempt delay");
    public static Setting stX = new Setting(Mode.INTEGER, "StX", 0, "Station X");
    public static Setting stY = new Setting(Mode.INTEGER, "StY", 0, "Station Y");
    public static Setting stZ = new Setting(Mode.INTEGER, "StZ", 0, "Station Z");
    public static Setting rX = new Setting(Mode.INTEGER, "rX", 0, "Reset X");
    public static Setting rY = new Setting(Mode.INTEGER, "rY", 0, "Reset Y");
    public static Setting rZ = new Setting(Mode.INTEGER, "rZ", 0, "Reset Z");
    public static Setting leastFirst = new Setting(Mode.BOOLEAN, "LeastFirst", false, "Place least used carpets first");
    public static Setting instantChest = new Setting(Mode.BOOLEAN, "InstChest", true, "Instant chest extraction");
    public static Setting carpetDelay = new Setting(Mode.DOUBLE, "CarpetDelay", new SettingValue(0.05, 0, 2, 0.01), "Delay between dropping carpets");
    public static Setting speedMultiplier = new Setting(Mode.DOUBLE, "SpeedMul", new SettingValue(1.0, 0.01, 2.0, 0.001), "Speed multiplier for delays; lower is faster");

    // Instance variables
    public SchematicReader sr;
    public Map<BlockPos, BlockState> bc = new HashMap<>();
    public Timer bct = new Timer();
    public Block btp;
    public Map<BlockPos, Integer> ap = new HashMap<>();
    public Timer aprt = new Timer(), cot = new Timer(), frt = new Timer();
    public boolean init, ea, fin, rm;

    // Throttle repeated chat messages
    private long lastWrongCarpetMsg = 0;
    private long lastOpenChestMsg   = 0;
    private static final long MESSAGE_COOLDOWN = 2000;

    public AutoMapart() {
        super(Group.WORLD, "AutoMapart", "Automated carpet mapart bot for bewitched",
              "Schematics in /schematics named mapart-#.nbt", "Builds sequentially, locks map, then resets with water");
        WorldRenderEvents.BEFORE_ENTITIES.register(this::renderLast);
        new Thread(() -> {
            while (true) {
                try {
                    loop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sleep(5);
            }
        }).start();
    }

    // Helper method: sleep using the speed multiplier
    private void sleepOptimized(double seconds) {
        sleep((int)(seconds * speedMultiplier.asDouble() * 1000));
    }

    // Helper method: squared distance between two BlockPos
    private double squaredDistance(BlockPos a, BlockPos b) {
        int dx = a.getX() - b.getX();
        int dy = a.getY() - b.getY();
        int dz = a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    // Helper method: check if the player's crosshair is over a chest
    private boolean isLookingAtChest() {
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hit = (BlockHitResult) mc.crosshairTarget;
            Block blockTarget = mc.world.getBlockState(hit.getBlockPos()).getBlock();
            return (blockTarget == Blocks.CHEST || blockTarget == Blocks.TRAPPED_CHEST);
        }
        return false;
    }

    // Helper method: throttle a debug message
    private void throttledMessage(String text, long lastMsgTimeRef) {
        if (!debug.bool()) return;
        long now = System.currentTimeMillis();
        if (now - lastMsgTimeRef >= MESSAGE_COOLDOWN) {
            sendMessage(text);
        }
    }

    // ===== NEW PATHFINDING ENHANCEMENTS (with A*) =====

    private void safeGoTo(BlockPos targetPos) {
        BlockPos playerPos = mc.player.getBlockPos();
        targetPos = new BlockPos(targetPos.getX(), playerPos.getY(), targetPos.getZ());
        
        List<BlockPos> path = findPath(playerPos, targetPos);
        if (path.isEmpty()) {
            BlockPos safeTarget = findSafeTarget(targetPos);
            BaritoneUtils.goTo(safeTarget);
        } else {
            for (BlockPos waypoint : path) {
                BaritoneUtils.goTo(waypoint);
            }
        }
    }

    private List<BlockPos> findPath(BlockPos start, BlockPos target) {
        Comparator<Node> comparator = Comparator.comparingDouble(n -> n.fCost);
        PriorityQueue<Node> openSet = new PriorityQueue<>(comparator);
        Map<BlockPos, Double> costSoFar = new HashMap<>();
        Map<BlockPos, BlockPos> cameFrom = new HashMap<>();

        Node startNode = new Node(start, 0, heuristic(start, target));
        openSet.add(startNode);
        costSoFar.put(start, 0.0);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (current.pos.equals(target)) {
                return reconstructPath(cameFrom, target);
            }
            for (BlockPos neighbor : getNeighbors(current.pos)) {
                if (!isTraversable(mc.world.getBlockState(neighbor), neighbor)) continue;
                double newCost = costSoFar.get(current.pos) + 1;
                if (!costSoFar.containsKey(neighbor) || newCost < costSoFar.get(neighbor)) {
                    costSoFar.put(neighbor, newCost);
                    double priority = newCost + heuristic(neighbor, target);
                    openSet.add(new Node(neighbor, newCost, priority));
                    cameFrom.put(neighbor, current.pos);
                }
            }
        }
        return new ArrayList<>();
    }

    private double heuristic(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) + Math.abs(a.getZ() - b.getZ());
    }

    private List<BlockPos> reconstructPath(Map<BlockPos, BlockPos> cameFrom, BlockPos target) {
        LinkedList<BlockPos> path = new LinkedList<>();
        BlockPos current = target;
        while (cameFrom.containsKey(current)) {
            path.addFirst(current);
            current = cameFrom.get(current);
        }
        path.addFirst(current);
        return path;
    }

    private List<BlockPos> getNeighbors(BlockPos pos) {
        List<BlockPos> neighbors = new ArrayList<>();
        neighbors.add(pos.up());
        neighbors.add(pos.down());
        neighbors.add(pos.north());
        neighbors.add(pos.south());
        neighbors.add(pos.east());
        neighbors.add(pos.west());
        return neighbors;
    }

    private class Node {
        BlockPos pos;
        double cost;
        double fCost;

        public Node(BlockPos pos, double cost, double fCost) {
            this.pos = pos;
            this.cost = cost;
            this.fCost = fCost;
        }
    }

    private BlockPos findSafeTarget(BlockPos target) {
        int radius = 2;
        for (int r = 0; r <= radius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        BlockPos candidate = target.add(dx, dy, dz);
                        if (isPathSafe(candidate)) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return target;
    }

    private boolean isPathSafe(BlockPos targetPos) {
        BlockPos start = mc.player.getBlockPos();
        for (BlockPos pos : getLinePoints(start, targetPos)) {
            BlockState state = mc.world.getBlockState(pos);
            if (!isTraversable(state, pos)) {
                return false;
            }
        }
        return true;
    }

    private List<BlockPos> getLinePoints(BlockPos start, BlockPos end) {
        List<BlockPos> points = new ArrayList<>();
        int dx = end.getX() - start.getX();
        int dy = end.getY() - start.getY();
        int dz = end.getZ() - start.getZ();
        int steps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            int x = start.getX() + (int) Math.round(dx * t);
            int y = start.getY() + (int) Math.round(dy * t);
            int z = start.getZ() + (int) Math.round(dz * t);
            points.add(new BlockPos(x, y, z));
        }
        return points;
    }

    private boolean isTraversable(BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof CarpetBlock) {
            return true;
        }
        return state.getCollisionShape(mc.world, pos).isEmpty();
    }

    private List<BlockPos> getAdjacentSafePositions(BlockPos chestPos) {
        List<BlockPos> safePositions = new ArrayList<>();
        BlockPos[] neighbors = new BlockPos[] {
            chestPos.north(),
            chestPos.south(),
            chestPos.east(),
            chestPos.west()
        };
        for (BlockPos pos : neighbors) {
            if (mc.world.getBlockState(pos).getBlock() == Blocks.AIR &&
                mc.world.getBlockState(pos.up()).getBlock() == Blocks.AIR) {
                safePositions.add(pos);
            }
        }
        return safePositions;
    }

    // ===== INVENTORY MANAGEMENT REFINEMENTS =====

    private void dropWrongCarpets() {
        List<Slot> slots = mc.player.playerScreenHandler.slots;
        for (Slot slot : slots) {
            if (slot.getStack() == null) continue;
            if (slot.getStack().getItem() instanceof ArmorItem) continue;
            if (Block.getBlockFromItem(slot.getStack().getItem()) instanceof CarpetBlock
                    && slot.getStack().getItem() != Item.fromBlock(btp)) {
                run(() -> InventoryUtils.dropItem(slot.id));
                sleepOptimized(carpetDelay.asDouble());
            }
        }
    }

    private void takeCarpetsFromChest() {
        sleepOptimized(0.3);
        double delay = instantChest.bool() ? carpetDelay.asDouble() : takeDelay.asDouble();
        List<Slot> slots = mc.player.currentScreenHandler.slots;
        for (Slot slot : slots) {
            if (slot.id <= 53 && slot.getStack().getItem() == Item.fromBlock(btp)) {
                if (InventoryUtils.getFreeSpace() <= 0) break;
                run(() -> InventoryUtils.quickMove(slot.id));
                sleepOptimized(delay);
            }
        }
        run(() -> mc.currentScreen.close());
    }

    // ===== END INVENTORY MANAGEMENT REFINEMENTS =====

    @Override
    public void onEnabled() {
        try {
            File f = new File(mc.runDirectory.getPath() + "/schematics/mapart-" + sIdx.asInt() + ".nbt");
            if (!f.exists()) {
                if (debug.bool()) sendMessage("No schematic " + sIdx.asInt() + " found, exiting", Formatting.RED);
                disable();
                return;
            }
            if (resetCache.bool()) bc.clear();
            bct.ms = 0;
            btp = null;
            ap.clear();
            fin = false;
            rm = false;
            sr = new SchematicReader();
            sr.load(f);
            if (debug.bool()) {
                sendMessage("Loaded " + f.getName() + " with " + sr.list.size() + " blocks");
            }
            int purged = 0;
            List<SchematicReader.SchematicBlock> tmp = new ArrayList<>(sr.list);
            for (SchematicReader.SchematicBlock sb : tmp) {
                if (!(sb.block instanceof CarpetBlock)) {
                    purged++;
                    sr.list.remove(sb);
                } else {
                    switch (sDir.asInt()) {
                        case 1:
                            sb.pos = new BlockPos(sb.pos.getX(), sb.pos.getY(), -sb.pos.getZ());
                            break;
                        case 2:
                            sb.pos = new BlockPos(-sb.pos.getX(), sb.pos.getY(), sb.pos.getZ());
                            break;
                        case 3:
                            sb.pos = new BlockPos(-sb.pos.getX(), sb.pos.getY(), -sb.pos.getZ());
                            break;
                        case 4:
                            break;
                        case 5:
                            sb.pos = new BlockPos(sb.pos.getZ(), sb.pos.getY(), -sb.pos.getX());
                            break;
                        case 6:
                            sb.pos = new BlockPos(-sb.pos.getZ(), sb.pos.getY(), sb.pos.getX());
                            break;
                        case 7:
                            sb.pos = new BlockPos(-sb.pos.getZ(), sb.pos.getY(), -sb.pos.getX());
                            break;
                        case 8:
                            sb.pos = new BlockPos(sb.pos.getZ(), sb.pos.getY(), sb.pos.getX());
                            break;
                    }
                }
            }
            if (purged > 0 && debug.bool()) {
                sendMessage("Purged " + purged + " non-carpet blocks");
            }
            init = true;
        } catch (Exception e) {
            if (debug.bool()) {
                sendMessage("Failed to load schematic", Formatting.RED);
            }
            e.printStackTrace();
        }
    }

    @Override
    public void onDisabled() {
        init = false;
    }

    @Subscribe
    public void renderLast(WorldRenderContext ctx) {
        if (isOn() && render.bool()) {
            int offX = sX.asInt(), offY = sY.asInt(), offZ = sZ.asInt();
            Vec3d cam = mc.gameRenderer.getCamera().getPos();
            for (SchematicReader.SchematicBlock sb : sr.list) {
                ctx.matrixStack().push();
                BlockPos pos = sb.pos.add(offX, offY, offZ);
                ctx.matrixStack().translate(pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z);
                mc.getBlockRenderManager().renderBlockAsEntity(sb.block.getDefaultState(), ctx.matrixStack(),
                        mc.getBufferBuilders().getEntityVertexConsumers(), 0xF000F0, OverlayTexture.DEFAULT_UV);
                ctx.matrixStack().pop();
            }
        }
    }

    public void loop() {
        if (mc.player == null && isOn()) ea = true;
        if (!isOn() || mc.player == null || mc.world == null || render.bool() || !init) {
            return;
        }
        sleep(150);
        if (ea) {
            ea = false;
            run(this::onEnabled);
            sleep(5);
        }
        BlockPos playerPos = mc.player.getBlockPos();
        int offX = sX.asInt(), offY = sY.asInt(), offZ = sZ.asInt();
        double maxPlaceDist = pDist.asDouble();
        double maxDistSq = maxPlaceDist * maxPlaceDist;

        if (fin) {
            sleep(200);
            if (rm) {
                BlockPos rp = new BlockPos(rX.asInt(), rY.asInt(), rZ.asInt());
                if (squaredDistance(playerPos, rp) > 1) {
                    if (debug.bool()) sendMessage("Moving to reset button");
                    safeGoTo(rp);
                } else {
                    if (debug.bool()) sendMessage("Pressing reset button");
                    BlockPos btnPos = BlockUtils.findBlock(new Vec3d(playerPos.getX(), playerPos.getY(), playerPos.getZ()), Blocks.OAK_BUTTON, 4);
                    run(() -> RotationUtils.rotateTo(new BlockPos(btnPos.getX(), btnPos.getY() + 1, btnPos.getZ()), 0, Direction.DOWN));
                    if (frt.hasPassed(1000)) {
                        run(PlayerUtils::rightClick);
                        frt.reset();
                    }
                    sIdx.setValue(sIdx.asInt() + 1);
                    Settings.saveSettings();
                    if (debug.bool()) {
                        sendMessage("Finished mapart, increased index, waiting while water flows");
                    }
                    run(this::onEnabled);
                }
            } else {
                if (mc.player.playerScreenHandler != null) {
                    if (debug.bool()) sendMessage("Dropping carpets");
                    run(() -> RotationUtils.rotateTo(playerPos.add(2, 1, 0)));
                    dropWrongCarpets();
                    return;
                }
                BlockPos sp = new BlockPos(stX.asInt(), stY.asInt(), stZ.asInt());
                boolean locked = false;
                for (Slot slot : mc.player.playerScreenHandler.slots) {
                    if (slot.getStack().getItem() == Items.FILLED_MAP) {
                        ItemStack st = slot.getStack();
                        if (st.hasNbt() && st.getNbt().contains("map")) {
                            int mapId = st.getNbt().getInt("map");
                            MapState ms = ((FilledMapItem) st.getItem()).getMapState(mapId, mc.world);
                            if (ms != null && ms.locked) {
                                locked = true;
                                break;
                            }
                        }
                    }
                }
                if (locked) {
                    if (mc.currentScreen instanceof GenericContainerScreen) {
                        if (debug.bool()) sendMessage("Taking carpets from chest");
                        takeCarpetsFromChest();
                    } else {
                        if (debug.bool()) sendMessage("No chest open, looking for chest or station container");
                    }
                } else if (mc.currentScreen instanceof CartographyTableScreen) {
                    if (debug.bool()) sendMessage("Locking map");
                    List<Slot> slots = mc.player.playerScreenHandler.slots;
                    for (Slot slot : slots) {
                        if (slot.id > 2 && (slot.getStack().getItem() == Items.GLASS_PANE
                                || slot.getStack().getItem() == Items.FILLED_MAP)) {
                            run(() -> InventoryUtils.quickMove(slot.id));
                        }
                    }
                    run(() -> InventoryUtils.quickMove(2));
                    run(() -> mc.currentScreen.close());
                } else if (squaredDistance(playerPos, sp) > 1) {
                    if (debug.bool()) sendMessage("Mapart finished, moving to station");
                    safeGoTo(sp);
                } else if ((InventoryUtils.getAmountOfItem(Items.MAP) == 0
                        && InventoryUtils.getAmountOfItem(Items.FILLED_MAP) == 0)
                        || InventoryUtils.getAmountOfItem(Items.GLASS_PANE) == 0) {
                    if (debug.bool()) sendMessage("No map/glass, clicking button");
                    BlockPos btnPos = BlockUtils.findBlock(new Vec3d(playerPos.getX(), playerPos.getY(), playerPos.getZ()),
                            Blocks.OAK_BUTTON, 2);
                    run(() -> RotationUtils.rotateTo(new BlockPos(btnPos.getX(), btnPos.getY() - 1, btnPos.getZ())));
                    if (frt.hasPassed(1000)) {
                        run(PlayerUtils::rightClick);
                        frt.reset();
                    }
                } else if (InventoryUtils.getAmountOfItem(Items.FILLED_MAP) == 0) {
                    if (debug.bool()) sendMessage("Filling empty map");
                    run(() -> InventoryUtils.switchToItem(Items.MAP));
                    run(() -> mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND));
                } else {
                    if (debug.bool()) sendMessage("Opening cartography table");
                    BlockPos ctPos = BlockUtils.findBlock(new Vec3d(playerPos.getX(), playerPos.getY(), playerPos.getZ()),
                            Blocks.CARTOGRAPHY_TABLE, 2);
                    run(() -> RotationUtils.rotateTo(new BlockPos(ctPos.getX(), ctPos.getY(), ctPos.getZ())));
                    if (frt.hasPassed(1000)) {
                        run(PlayerUtils::rightClick);
                        frt.reset();
                    }
                }
                return;
            }
            return;
        }

        if (aprt.hasPassed((int) (resetAttemptDelay.asDouble() * 1000))) {
            ap.clear();
            aprt.reset();
        }
        if (bct.hasPassed((int) (cacheDelay.asDouble() * 1000))) {
            bct.reset();
            for (SchematicReader.SchematicBlock sb : sr.list) {
                BlockPos pos = sb.pos.add(offX, offY, offZ);
                BlockState st = mc.world.getBlockState(pos);
                if (st.getBlock() != Blocks.VOID_AIR) {
                    bc.put(pos, st);
                }
            }
        }
        if (bc.size() < 14000) {
            if (debug.bool()) {
                sendMessage("Low render, mapping schematic blocks: " + bc.size());
            }
            for (SchematicReader.SchematicBlock sb : sr.list) {
                BlockPos pos = sb.pos.add(offX, offY, offZ);
                if (!bc.containsKey(pos)) {
                    safeGoTo(pos);
                    return;
                }
            }
        }
        if (btp == null) {
            Map<Block, Integer> cnt = new HashMap<>();
            for (SchematicReader.SchematicBlock sb : sr.list) {
                BlockPos pos = sb.pos.add(offX, offY, offZ);
                if (bc.containsKey(pos) && bc.get(pos).getBlock() != Blocks.AIR) {
                    continue;
                }
                Block b = sb.block;
                cnt.put(b, cnt.getOrDefault(b, 0) + 1);
            }
            if (cnt.isEmpty()) {
                fin = true;
                return;
            }
            btp = leastFirst.bool()
                    ? cnt.entrySet().stream().min(Map.Entry.comparingByValue()).get().getKey()
                    : cnt.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
            if (debug.bool()) {
                sendMessage("Now placing " + btp.getName().getString() + " x" + cnt.get(btp));
            }
        }

        boolean rot = false;
        for (Slot slot : mc.player.playerScreenHandler.slots) {
            if (slot.getStack().getItem() instanceof ArmorItem) continue;
            if (Block.getBlockFromItem(slot.getStack().getItem()) instanceof CarpetBlock) {
                if (slot.getStack().getItem() != Item.fromBlock(btp)) {
                    if (!rot && debug.bool()) {
                        long now = System.currentTimeMillis();
                        if (now - lastWrongCarpetMsg >= MESSAGE_COOLDOWN) {
                            sendMessage("Throwing wrong carpets");
                            lastWrongCarpetMsg = now;
                        }
                        rot = true;
                        run(() -> RotationUtils.rotateTo(playerPos.add(2, 1, 0)));
                    }
                    run(() -> InventoryUtils.throwAway(slot.id));
                    sleepOptimized(carpetDelay.asDouble());
                }
            }
        }
        if (rot) {
            run(() -> mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId)));
        }

        if (InventoryUtils.getAmountOfItem(Item.fromBlock(btp)) == 0) {
            List<BlockPos> chests = new ArrayList<>();
            for (int x = -200; x < 200; x++) {
                for (int z = -200; z < 200; z++) {
                    for (int y = -5; y < 5; y++) {
                        BlockPos pos = playerPos.add(x, y, z);
                        BlockState st = mc.world.getBlockState(pos);
                        if ((st.getBlock() == Blocks.CHEST || st.getBlock() == Blocks.TRAPPED_CHEST)
                                && mc.world.getBlockState(pos.add(0, 1, 0)).getBlock() == btp) {
                            chests.add(pos);
                        }
                    }
                }
            }
            if (!chests.isEmpty()) {
                BlockPos nc = Collections.min(chests, Comparator.comparingDouble(pos -> squaredDistance(playerPos, pos)));
                if (squaredDistance(playerPos, nc) <= 16) {
                    if (mc.currentScreen instanceof GenericContainerScreen) {
                        if (debug.bool()) sendMessage("Taking carpets from chest");
                        takeCarpetsFromChest();
                    } else {
                        long now = System.currentTimeMillis();
                        if (now - lastOpenChestMsg >= MESSAGE_COOLDOWN) {
                            if (debug.bool()) {
                                sendMessage("Opening chest at " + nc);
                            }
                            lastOpenChestMsg = now;
                        }
                        run(() -> RotationUtils.rotateTo(nc, -0.3));
                        sleep(150);
                        if (cot.hasPassed(1000) && isLookingAtChest()) {
                            run(PlayerUtils::rightClick);
                            cot.reset();
                        }
                    }
                } else {
                    if (debug.bool()) sendMessage("Moving to chest at " + nc);
                    List<BlockPos> safePositions = getAdjacentSafePositions(nc);
                    if (!safePositions.isEmpty()) {
                        BlockPos bestPos = Collections.min(safePositions, Comparator.comparingDouble(pos -> squaredDistance(playerPos, pos)));
                        safeGoTo(bestPos);
                    } else {
                        safeGoTo(nc);
                    }
                }
                return;
            }
            if (debug.bool()) {
                sendMessage("No chest found with " + btp.getName().getString());
            }
            return;
        }

        List<BlockPos> posList = new ArrayList<>();
        for (SchematicReader.SchematicBlock sb : sr.list) {
            BlockPos pos = sb.pos.add(offX, offY, offZ);
            if (pos.equals(playerPos)
                    || sb.block != btp
                    || (squaredDistance(playerPos, pos) <= 625 && mc.world.getBlockState(pos).getBlock() != Blocks.AIR)
                    || ap.containsKey(pos)) {
                continue;
            }
            if (squaredDistance(playerPos, pos) <= maxDistSq) {
                posList.add(pos);
                ap.put(pos, ap.getOrDefault(pos, 0) + 1);
            }
        }
        if (posList.isEmpty()) {
            BlockPos clo = null;
            for (SchematicReader.SchematicBlock sb : sr.list) {
                BlockPos pos = sb.pos.add(offX, offY, offZ);
                if (squaredDistance(playerPos, pos) <= 4
                        || sb.block != btp
                        || ap.containsKey(pos)) {
                    continue;
                }
                if (bc.containsKey(pos) && bc.get(pos).getBlock() == Blocks.AIR) {
                    if (clo == null || squaredDistance(playerPos, pos) < squaredDistance(playerPos, clo)) {
                        clo = pos;
                    }
                }
            }
            if (clo == null) {
                btp = null;
                return;
            }
            if (mc.world.getBlockState(clo.add(1, 0, 0)).getBlock() == btp) {
                clo = clo.add(1, 0, 0);
            } else if (mc.world.getBlockState(clo.add(-1, 0, 0)).getBlock() == btp) {
                clo = clo.add(-1, 0, 0);
            } else if (mc.world.getBlockState(clo.add(0, 0, 1)).getBlock() == btp) {
                clo = clo.add(0, 0, 1);
            } else if (mc.world.getBlockState(clo.add(0, 0, -1)).getBlock() == btp) {
                clo = clo.add(0, 0, -1);
            } else {
                int xDir = Integer.compare(playerPos.getX(), clo.getX());
                int zDir = Integer.compare(playerPos.getZ(), clo.getZ());
                clo = clo.add(xDir, 0, zDir);
            }
            sendMessage("Moving to closest spot to place " + btp.getName().getString());
            safeGoTo(clo);
        } else {
            sendMessage("Placing " + posList.size() + " " + btp.getName().getString() + " around player");
            posList.sort(Comparator.comparingInt(pos -> (int) squaredDistance(playerPos, pos)));
            Collections.reverse(posList);
            for (BlockPos pos : posList) {
                if (InventoryUtils.getHotbarSlot(Item.fromBlock(btp)) != mc.player.getInventory().selectedSlot) {
                    run(() -> InventoryUtils.switchToItem(Item.fromBlock(btp)));
                }
                run(() -> RotationUtils.rotateTo(pos.add(0, -1, 0)));
                sleep((int)(rotWait.asDouble() * 1000));
                if (mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult hit = (BlockHitResult) mc.crosshairTarget;
                    if (mc.world.getBlockState(hit.getBlockPos()).getBlock() == btp) {
                        continue;
                    }
                }
                run(PlayerUtils::rightClick);
                sleep((int)(pSpeed.asDouble() * 1000));
            }
        }
    }
}
