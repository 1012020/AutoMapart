package me.bebeli555.automapart.utils;

import me.bebeli555.automapart.Mod;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

public class EntityUtils extends Mod {
    private static StatusEffectInstance[] effects = {
            new StatusEffectInstance(StatusEffects.WEAKNESS, 1, 0),
            new StatusEffectInstance(StatusEffects.STRENGTH, 1, 0),
            new StatusEffectInstance(StatusEffects.STRENGTH, 1, 1),
            new StatusEffectInstance(StatusEffects.REGENERATION, 1, 0),
            new StatusEffectInstance(StatusEffects.REGENERATION, 1, 1),
            new StatusEffectInstance(StatusEffects.POISON, 1, 0),
            new StatusEffectInstance(StatusEffects.POISON, 1, 1),
            new StatusEffectInstance(StatusEffects.WATER_BREATHING, 1, 0),
            new StatusEffectInstance(StatusEffects.INVISIBILITY, 1, 0),
            new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1, 0),
            new StatusEffectInstance(StatusEffects.JUMP_BOOST, 1, 0),
            new StatusEffectInstance(StatusEffects.JUMP_BOOST, 1, 1),
            new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 1, 0),
            new StatusEffectInstance(StatusEffects.SPEED, 1, 0),
            new StatusEffectInstance(StatusEffects.SPEED, 1, 1),
            new StatusEffectInstance(StatusEffects.SLOWNESS, 1, 0),
            new StatusEffectInstance(StatusEffects.SLOWNESS, 1, 3),
            new StatusEffectInstance(StatusEffects.SLOWNESS, 1, 5),
            new StatusEffectInstance(StatusEffects.RESISTANCE, 1, 0),
            new StatusEffectInstance(StatusEffects.RESISTANCE, 1, 3),
            new StatusEffectInstance(StatusEffects.ABSORPTION, 1, 0),
            new StatusEffectInstance(StatusEffects.ABSORPTION, 1, 3),
            new StatusEffectInstance(StatusEffects.HASTE, 1, 0),
            new StatusEffectInstance(StatusEffects.HASTE, 1, 1)
    };

    private static Map<Integer, List<StatusEffectInstance>> effectColorMap = new HashMap<>();
    private static Map<LivingEntity, Integer> lastEntityColors = new HashMap<>();

	/**
	 * Checks if the entity is passive. Like an animal
	 */
    public static boolean isPassive(Entity e) {
        return !isNeutralMob(e) && !isHostileMob(e);
    }
    
    /**
     * The mob won't attack player but will if player attacks it
     */
    public static boolean isNeutralMob(Entity entity) {
        return entity instanceof WolfEntity || entity instanceof EndermanEntity || entity instanceof ZombifiedPiglinEntity;
    }
    
    /**
     * Is the mob hostile. Like will always try to attack player
     */
    public static boolean isHostileMob(Entity entity) {
        return (entity instanceof Monster) && !EntityUtils.isNeutralMob(entity);
    }

    /**
     * Gets all entities without the client player included
     */
    public static List<Entity> getAllWithoutClientPlayer() {
        return getAll().stream().filter(entity -> entity != mc.player).toList();
    }

    /**
     * Reverse engineers list of status effects from the potion color the entity has
     */
    public static List<StatusEffectInstance> getEffectsFromColor(LivingEntity entity) {
        int color = entity.dataTracker.get(LivingEntity.POTION_SWIRLS_COLOR);
        if (color == 0) {
            return new ArrayList<>();
        }

        List<StatusEffectInstance> check = effectColorMap.get(color);
        if (check != null) {
            lastEntityColors.put(entity, color);
            return check;
        }

        Integer lastColor = lastEntityColors.get(entity);
        if (lastColor != null) {
            effectColorMap.put(color, effectColorMap.get(lastColor));
        } else {
            effectColorMap.put(color, new ArrayList<>());
        }

        new Thread(() -> {
            int numPotions = effects.length;
            int totalCombinations = 1 << numPotions;

            // Loop through all possible combinations
            outer: for (int i = 0; i < totalCombinations; i++) {
                List<StatusEffectInstance> combination = new ArrayList<>();

                // Check which potions are included in this combination based on the binary representation
                for (int j = 0; j < numPotions; j++) {
                    if ((i & (1 << j)) != 0) {
                        combination.add(effects[j]);
                    }
                }

                if (PotionUtil.getColor(combination) == color) {
                    //Cant have multiple amplifiers of same effect
                    for (StatusEffectInstance instance : combination) {
                        long count = combination.stream().filter(e -> e.getEffectType() == instance.getEffectType()).count();
                        if (count > 1) {
                            continue outer;
                        }
                    }

                    effectColorMap.put(color, combination);
                    return;
                }
            }

            effectColorMap.put(color, new ArrayList<>());
        }).start();

        return effectColorMap.get(color);
    }

    /**
     * Gets a list of all the entities loaded in the world
     */
    public static List<Entity> getAll() {
        return StreamSupport.stream(mc.world.getEntities().spliterator(), false).toList();
    }

    /**
     * Gets all the loaded block entities in the world like chests, droppers etc
     */
    public static List<BlockEntity> getAllBlockEntities() {
        List<BlockEntity> list = new ArrayList<>();
        for (WorldChunk chunk : WorldUtils.getLoadedChunks()) {
            list.addAll(chunk.getBlockEntities().values());
        }

        return list;
    }

    /**
     * Offsets the prev pos of the entity for the vector using tickDelta for smooth rendering
     */
    public static Vec3d offsetLastTickPos(Entity entity, float tickDelta) {
        return RenderUtils3D.offsetLastTickPos(entity.getPos(), new Vec3d(entity.prevX, entity.prevY, entity.prevZ), tickDelta);
    }

    /**
     * Gets the current speed of this entity by looking at the distance between prev pos
     */
    public static double getSpeed(Entity entity) {
        return new Vec3d(entity.getX(), entity.getY(), entity.getZ()).distanceTo(new Vec3d(entity.prevX, entity.prevY, entity.prevZ));
    }
}
