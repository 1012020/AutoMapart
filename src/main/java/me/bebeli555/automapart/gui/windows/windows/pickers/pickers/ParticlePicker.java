package me.bebeli555.automapart.gui.windows.windows.pickers.pickers;

import me.bebeli555.automapart.gui.windows.windows.pickers.PickerWindow;
import me.bebeli555.automapart.settings.Setting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ParticlePicker extends PickerWindow {
    public static ParticlePicker INSTANCE;

    public ParticlePicker() {
        super("ParticlePicker");
        INSTANCE = this;

        for (DefaultParticleType particle : getParticles()) {
            String name = StringUtils.capitalize(particle.asString().replace("minecraft:", "").replace("_", " "));
            add(new PickerItem(name, getItemForParticle(particle, name), "", particle));
        }
    }

    public static Object getItemForParticle(DefaultParticleType particleType, String name) {
        if (particleType == ParticleTypes.AMBIENT_ENTITY_EFFECT) {
            return Items.PHANTOM_MEMBRANE;
        } else if (particleType == ParticleTypes.ANGRY_VILLAGER) {
            return StatusEffects.BAD_OMEN;
        } else if (particleType == ParticleTypes.BUBBLE) {
            return StatusEffects.WATER_BREATHING;
        } else if (particleType == ParticleTypes.CLOUD) {
            return StatusEffects.LEVITATION;
        } else if (particleType == ParticleTypes.CRIT) {
            return Items.DIAMOND_SWORD;
        } else if (particleType == ParticleTypes.DAMAGE_INDICATOR) {
            return Items.REDSTONE;
        } else if (particleType == ParticleTypes.DRAGON_BREATH) {
            return Items.DRAGON_BREATH;
        } else if (particleType == ParticleTypes.ELECTRIC_SPARK) {
            return Items.GLOWSTONE_DUST;
        } else if (particleType == ParticleTypes.ENCHANTED_HIT) {
            return Items.ENCHANTED_BOOK;
        } else if (particleType == ParticleTypes.ENCHANT) {
            return Items.ENCHANTING_TABLE;
        } else if (particleType == ParticleTypes.END_ROD) {
            return Items.END_ROD;
        } else if (particleType == ParticleTypes.EXPLOSION) {
            return Items.TNT;
        } else if (particleType == ParticleTypes.FIREWORK) {
            return Items.FIREWORK_ROCKET;
        } else if (particleType == ParticleTypes.FISHING) {
            return Items.FISHING_ROD;
        } else if (particleType == ParticleTypes.FLAME) {
            return Items.FLINT_AND_STEEL;
        } else if (particleType == ParticleTypes.HEART) {
            return StatusEffects.INSTANT_HEALTH;
        } else if (particleType == ParticleTypes.INSTANT_EFFECT) {
            return Items.POTION;
        } else if (particleType == ParticleTypes.LARGE_SMOKE) {
            return Items.CAMPFIRE;
        } else if (particleType == ParticleTypes.LAVA) {
            return Items.LAVA_BUCKET;
        } else if (particleType == ParticleTypes.MYCELIUM) {
            return Items.MYCELIUM;
        } else if (particleType == ParticleTypes.NAUTILUS) {
            return Items.NAUTILUS_SHELL;
        } else if (particleType == ParticleTypes.NOTE) {
            return Items.NOTE_BLOCK;
        } else if (particleType == ParticleTypes.POOF) {
            return Items.FEATHER;
        } else if (particleType == ParticleTypes.PORTAL) {
            return Items.ENDER_EYE;
        } else if (particleType == ParticleTypes.RAIN) {
            return Items.WATER_BUCKET;
        } else if (particleType == ParticleTypes.SMOKE) {
            return Items.TORCH;
        } else if (particleType == ParticleTypes.SNEEZE) {
            return Items.PAPER;
        } else if (particleType == ParticleTypes.SOUL) {
            return Items.SOUL_TORCH;
        } else if (particleType == ParticleTypes.SPIT) {
            return Items.ARROW;
        } else if (particleType == ParticleTypes.EFFECT) {
            return Items.BOOK;
        } else if (particleType == ParticleTypes.ENTITY_EFFECT) {
            return Items.POTION;
        } else if (particleType == ParticleTypes.SQUID_INK) {
            return Items.INK_SAC;
        } else if (particleType == ParticleTypes.SWEEP_ATTACK) {
            return StatusEffects.STRENGTH;
        } else if (particleType == ParticleTypes.TOTEM_OF_UNDYING) {
            return Items.TOTEM_OF_UNDYING;
        } else if (particleType == ParticleTypes.WITCH) {
            return Items.POTION;
        } else if (particleType == ParticleTypes.WHITE_ASH) {
            return Items.WHITE_DYE;
        } else if (particleType == ParticleTypes.CHERRY_LEAVES) {
            return Items.CHERRY_LEAVES;
        } else if (particleType == ParticleTypes.ELDER_GUARDIAN) {
            return Items.ELDER_GUARDIAN_SPAWN_EGG;
        } else if (particleType == ParticleTypes.SPLASH) {
            return Items.SPLASH_POTION;
        } else if (particleType == ParticleTypes.DOLPHIN) {
            return Items.DOLPHIN_SPAWN_EGG;
        } else if (particleType == ParticleTypes.SCRAPE) {
            return Items.EMERALD;
        } else if (particleType == ParticleTypes.GLOW_SQUID_INK) {
            return Items.GLOW_INK_SAC;
        }

        name = name.toLowerCase();
        if (name.contains("obsidian")) {
            return Items.OBSIDIAN;
        } else if (name.contains("lava")) {
            return Items.LAVA_BUCKET;
        } else if (name.contains("water")) {
            return Items.WATER_BUCKET;
        } else if (name.contains("explosion")) {
            return Items.TNT;
        } else if (name.contains("egg")) {
            return Items.EGG;
        } else if (name.contains("portal")) {
            return Items.END_PORTAL_FRAME;
        } else if (name.contains("flame")) {
            return Items.FLINT_AND_STEEL;
        } else if (name.contains("snow")) {
            return Items.SNOWBALL;
        } else if (name.contains("honey")) {
            return Items.HONEYCOMB;
        } else if (name.contains("ink")) {
            return Items.INK_SAC;
        } else if (name.contains("villager")) {
            return StatusEffects.BAD_OMEN;
        } else if (name.contains("spore")) {
            return Items.SPORE_BLOSSOM;
        } else if (name.contains("slime")) {
            return Items.SLIME_BALL;
        } else if (name.contains("composter")) {
            return Items.COMPOSTER;
        } else if (name.contains("flash")) {
            return Items.GLOWSTONE;
        } else if (name.contains("sculk")) {
            return Items.SCULK;
        } else if (name.contains("boom")) {
            return Items.TNT;
        } else if (name.contains("bubble")) {
            return StatusEffects.WATER_BREATHING;
        } else if (name.contains("current")) {
            return Items.WATER_BUCKET;
        } else if (name.contains("campfire")) {
            return Items.CAMPFIRE;
        } else if (name.contains("glow")) {
            return Items.GLOWSTONE;
        } else if (name.contains("ash")) {
            return Items.COAL;
        } else if (name.contains("wax")) {
            return Items.HONEYCOMB;
        } else if (name.contains("nectar")) {
            return Items.BEEHIVE;
        }

        // If no matching particle is found, return a default item
        return Items.AIR;
    }

    public List<DefaultParticleType> getParticles() {
        List<DefaultParticleType> particleTypes = new ArrayList<>();

        try {
            for (Field field : ParticleTypes.class.getFields()) {
                Object value = field.get(null);
                if (value instanceof DefaultParticleType) {
                    particleTypes.add((DefaultParticleType) value);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return particleTypes;
    }

    public boolean isValid(ParticleType<?> effect, Setting setting) {
        try {
            List<PickerItem> selected = getSelectedFromSetting(setting);
            DefaultParticleType defaultParticleType = (DefaultParticleType)effect;

            for (PickerItem item : selected) {
                if (item.name.equals(StringUtils.capitalize(defaultParticleType.asString().replace("minecraft:", "").replace("_", " ")))) {
                    return true;
                }
            }
        } catch (Exception ignored) {}

        return false;
    }

    public static List<DefaultParticleType> getSelectedParticles(Setting setting) {
        List<DefaultParticleType> list = new ArrayList<>();
        for (PickerItem item : INSTANCE.getSelectedFromSetting(setting)) {
            list.add((DefaultParticleType)item.customObject);
        }

        return list;
    }
}
