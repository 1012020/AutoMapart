package me.bebeli555.automapart.gui.windows.windows.pickers.pickers;

import me.bebeli555.automapart.gui.windows.windows.pickers.PickerWindow;
import me.bebeli555.automapart.mods.ClientSettings;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.utils.EntityUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Items;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EntityTypePicker extends PickerWindow {
    public static EntityTypePicker INSTANCE;

    public static PickerItem itemsPickerItem;
    public static PickerItem projectilesPickerItem;

    public static List<Setting> visibleItemsSettings = new ArrayList<>();
    public static List<Setting> visibleProjectilesSettings = new ArrayList<>();

    public EntityTypePicker() {
        super("EntityTypePicker");
        INSTANCE = this;
        this.height = 105;
        this.width = 90;
        this.startY = 25;
        this.noWindowComponentRendering = true;
        this.noScroll = true;

        add(new PickerItem("Passive", StatusEffects.REGENERATION));
        add(new PickerItem("Neutral", StatusEffects.NIGHT_VISION));
        add(new PickerItem("Hostile", StatusEffects.BAD_OMEN));
        add(new PickerItem("Players", Items.PLAYER_HEAD));

        itemsPickerItem = new PickerItem("Items", Items.GRASS_BLOCK);
        itemsPickerItem.visible = false;
        add(itemsPickerItem);

        projectilesPickerItem = new PickerItem("Projectiles", Items.ARROW);
        projectilesPickerItem.visible = false;
        add(projectilesPickerItem);
    }

    @Override
    public void onEnabled(Setting setting) {
        itemsPickerItem.visible = visibleItemsSettings.contains(setting);
        projectilesPickerItem.visible = visibleProjectilesSettings.contains(setting);
    }

    @Override
    public void onDisabled() {
        itemsPickerItem.visible = false;
    }

    @Override
    public void onRender(DrawContext context) {
        this.height = (int)(list.stream().filter(i -> i.visible).count() * 26.25);
    }

    /**
     * Checks if the entity falls within the set values
     */
    public boolean isValid(Entity entity, Setting setting) {
        DefaultColor defaultColor = getDefaultColor(entity, setting);
        return !defaultColor.isDefault && defaultColor.contains;
    }

    public Color getColor(Entity entity, Setting setting, boolean mustContain) {
        DefaultColor defaultColor = getDefaultColor(entity, setting);
        if (!defaultColor.contains && mustContain) {
            return new Color(ClientSettings.entityTypeColorsDefault.asInt(), true);
        } else {
            return defaultColor.color;
        }
    }

    public DefaultColor getDefaultColor(Entity entity, Setting setting) {
        List<PickerItem> selected = getSelectedFromSetting(setting);

        if (entity instanceof ItemEntity) {
            return new DefaultColor(false, selected.contains(getPickerItemFromName("Items")), new Color(ClientSettings.entityTypeColorItems.asInt(), true));
        }

        if (entity instanceof ProjectileEntity) {
            return new DefaultColor(false, selected.contains(getPickerItemFromName("Projectiles")), new Color(ClientSettings.entityTypeColorProjectiles.asInt(), true));
        }

        if (entity instanceof PlayerEntity) {
            if (entity.equals(mc.getCameraEntity())) {
                return new DefaultColor(true, true, new Color(ClientSettings.entityTypeColorsDefault.asInt(), true));
            }

            return new DefaultColor(false, selected.contains(getPickerItemFromName("Players")), new Color(ClientSettings.entityTypeColorPlayers.asInt(), true));
        } else if (EntityUtils.isPassive(entity)) {
            return new DefaultColor(false, selected.contains(getPickerItemFromName("Passive")), new Color(ClientSettings.entityTypeColorPassive.asInt(), true));
        } else if (EntityUtils.isNeutralMob(entity)) {
            return new DefaultColor(false, selected.contains(getPickerItemFromName("Neutral")), new Color(ClientSettings.entityTypeColorNeutral.asInt(), true));
        } else if (EntityUtils.isHostileMob(entity)) {
            return new DefaultColor(false, selected.contains(getPickerItemFromName("Hostile")), new Color(ClientSettings.entityTypeColorHostile.asInt(), true));
        }

        return new DefaultColor(true, true, new Color(ClientSettings.entityTypeColorsDefault.asInt(), true));
    }

    public record DefaultColor(boolean isDefault, boolean contains, Color color) {}
}
