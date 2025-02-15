package me.bebeli555.automapart.utils;

import com.google.common.eventbus.Subscribe;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.block.BlockStateChangedEvent;
import me.bebeli555.automapart.utils.objects.TimeoutThread;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * Utility used for listening to chunk (block) loads and updates
 */
public class BlockLoadingUtil extends Utils {
    public BooleanSupplier activeCheck;
    public int checkAround;

    private Set<ChunkPos> lastCheckedChunks = new HashSet<>();

    /**
     * Creates the utility and starts running it
     * @param checkAround When a block is changed how many blocks around it to check too and process
     */
    public BlockLoadingUtil(int checkAround, BooleanSupplier activeCheck) {
        Mod.EVENT_BUS.register(this);

        this.activeCheck = activeCheck;
        this.checkAround = checkAround;

        new TimeoutThread() {
            public int onRun() {
                if (mc.world != null && activeCheck.getAsBoolean()) {
                    List<WorldChunk> chunks = WorldUtils.getLoadedChunks();
                    for (WorldChunk chunk : chunks) {
                        if (!lastCheckedChunks.contains(chunk.getPos())) {
                            processChunk(chunk);
                        }
                    }

                    lastCheckedChunks.clear();
                    for (WorldChunk chunk : chunks) {
                        lastCheckedChunks.add(chunk.getPos());
                    }
                } else {
                    lastCheckedChunks.clear();
                }

                return 100;
            }
        }.start();
    }

    private void processChunk(WorldChunk chunk) {
        for (int x = chunk.getPos().getStartX(); x < chunk.getPos().getStartX() + 16; x++) {
            for (int z = chunk.getPos().getStartZ(); z < chunk.getPos().getStartZ() + 16; z++) {
                for (int y = -65; y < (chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z) + 10); y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    loaded(pos, true);
                }
            }
        }
    }

    @Subscribe
    private void onBlockStateChanged(BlockStateChangedEvent event) {
        if (this.activeCheck.getAsBoolean()) {
            if (this.checkAround <= 0) {
                loaded(event.pos, false);
            } else {
                for (BlockPos pos : BlockUtils.getAll(BlockUtils.toVec(event.pos), this.checkAround, false)) {
                    loaded(pos, false);
                }
            }
        }
    }

    public void reset() {
        lastCheckedChunks.clear();
    }

    /**
     * Called when a block is first loaded or changed
     * @param loaded if false then it means the block has changed
     */
    public void loaded(BlockPos pos, boolean loaded) {}
}
