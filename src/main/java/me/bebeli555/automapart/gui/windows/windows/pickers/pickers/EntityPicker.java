package me.bebeli555.automapart.gui.windows.windows.pickers.pickers;

import com.google.common.eventbus.Subscribe;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.game.ClientTickEvent;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.gui.windows.components.ButtonComponent;
import me.bebeli555.automapart.gui.windows.windows.pickers.PickerWindow;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.utils.EntityUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.*;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.entity.projectile.thrown.*;
import net.minecraft.entity.vehicle.*;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.util.*;

public class EntityPicker extends PickerWindow {
    public static EntityPicker INSTANCE;

    public ButtonComponent entityTypePicker = new ButtonComponent(this, "Select groups");
    public Setting entityTypePickerSetting = new Setting(Mode.PICKER, "EntityTypePicker", "");
    public int[] customEntityPickerLoadPosition;

    public EntityPicker() {
        super("EntityPicker", 25);
        Mod.EVENT_BUS.register(this);
        INSTANCE = this;

        EntityTypePicker.visibleItemsSettings.add(entityTypePickerSetting);
        EntityTypePicker.visibleProjectilesSettings.add(entityTypePickerSetting);

        entityTypePicker.addClickListener(() -> {
            List<PickerItem> temp = new ArrayList<>(selected);
            String output = "Passive$Neutral$Hostile$Players$Projectiles$Items";

            String[] array = {"Passive", "Neutral", "Hostile", "Players", "Projectiles", "Items"};
            for (String s : array) {
                entityTypePickerSetting.setValue(s);
                if (addFromTypeSetting(true) != 0) {
                    output = output.replace("$" + s, "").replace(s + "$", "").replace(s, "");
                }

                selected = new ArrayList<>(temp);
            }

            entityTypePickerSetting.setValue(output);

            int loadX = Gui.lastMouseX;
            int loadY = Gui.lastMouseY;
            if (customEntityPickerLoadPosition != null) {
                loadX = customEntityPickerLoadPosition[0];
                loadY = customEntityPickerLoadPosition[1];
                customEntityPickerLoadPosition = null;
            }

            EntityTypePicker.INSTANCE.enable(entityTypePickerSetting, loadX, loadY);
        });

        EntityTypePicker.INSTANCE.addItemSelectedListener(new Setting.ValueChangedListener(){
            public void valueChanged(Object value) {
                if (EntityTypePicker.INSTANCE.currentSetting == entityTypePickerSetting) {
                    PickerItem item = (PickerItem)value;

                    EntityTypePicker.INSTANCE.selected.clear();
                    EntityTypePicker.INSTANCE.selected.add(item);
                    entityTypePickerSetting.setValue(item.name);

                    int added = addFromTypeSetting(true);
                    if (added == 0) {
                        addFromTypeSetting(false);
                    }

                    customEntityPickerLoadPosition = new int[]{EntityTypePicker.INSTANCE.x, EntityTypePicker.INSTANCE.y};
                    EntityTypePicker.INSTANCE.disable();
                    entityTypePickerSetting.setValue("");
                    updateSettings();

                    entityTypePicker.listeners.get(0).clicked(0);
                }
            }
        });
    }

    public int addFromTypeSetting(boolean add) {
        int added = 0;
        for (PickerItem item : EntityPicker.INSTANCE.list) {
            Entity entity = (Entity)item.customObject;
            if (entityTypePickerSetting.string().contains("Players")) {
                if (item.name.equals("Player")) {
                    if (add && !EntityPicker.INSTANCE.selected.contains(item)) {
                        EntityPicker.INSTANCE.selected.add(item);
                        added++;
                    } else if (!add && EntityPicker.INSTANCE.selected.contains(item)) {
                        EntityPicker.INSTANCE.selected.remove(item);
                        added++;
                    }
                }
            }

            if (entity != null && EntityTypePicker.INSTANCE.isValid(entity, entityTypePickerSetting)) {
                if (add && !EntityPicker.INSTANCE.selected.contains(item)) {
                    EntityPicker.INSTANCE.selected.add(item);
                    added++;
                } else if (!add && EntityPicker.INSTANCE.selected.contains(item)) {
                    EntityPicker.INSTANCE.selected.remove(item);
                    added++;
                }
            }
        }

        return added;
    }

    @Override
    public void onRender(DrawContext context) {
        if (!this.allowOnlyOneSelected) {
            entityTypePicker.customWidth = 51;
            entityTypePicker.render(context, 142, 59);
        }
    }

    @Subscribe
    public void onTick(ClientTickEvent e) {
        if (mc.world != null) {
            Mod.EVENT_BUS.unregister(this);

            for (EntityType<?> entityType : Registries.ENTITY_TYPE) {
                String name = entityType.getName().getString();
                Object object;

                Entity entity = entityType.create(mc.world);
                if (entity instanceof FishingBobberEntity) {
                    object = Items.FISHING_ROD;
                } else if (entity instanceof WitherSkullEntity) {
                    object = Items.WITHER_SKELETON_SKULL;
                } else if (entity instanceof TridentEntity) {
                    object = Items.TRIDENT;
                } else if (entity instanceof ChestMinecartEntity) {
                    object = Items.CHEST_MINECART;
                } else if (entity instanceof TntMinecartEntity) {
                    object = Items.TNT_MINECART;
                } else if (entity instanceof TntEntity) {
                    object = Items.TNT;
                } else if (entity instanceof DisplayEntity.TextDisplayEntity) {
                    object = Items.BOOK;
                } else if (entity instanceof SpectralArrowEntity) {
                    object = Items.SPECTRAL_ARROW;
                } else if (entity instanceof SpawnerMinecartEntity) {
                    object = Items.SPAWNER;
                } else if (entity instanceof SnowballEntity) {
                    object = Items.SNOWBALL;
                } else if (entity instanceof SmallFireballEntity) {
                    object = Items.FIRE_CHARGE;
                } else if (entity instanceof ShulkerBulletEntity) {
                    object = Items.ARROW;
                } else if (entity instanceof PotionEntity) {
                    object = Items.POTION;
                } else if (entity instanceof PaintingEntity) {
                    object = Items.PAINTING;
                } else if (entity instanceof CommandBlockMinecartEntity) {
                    object = Items.COMMAND_BLOCK_MINECART;
                } else if (entity instanceof FurnaceMinecartEntity) {
                    object = Items.FURNACE_MINECART;
                } else if (entity instanceof MinecartEntity) {
                    object = Items.MINECART;
                } else if (entity instanceof MarkerEntity) {
                    object = Items.MAP;
                } else if (entity instanceof LlamaSpitEntity) {
                    object = Items.ARROW;
                } else if (entity instanceof LightningEntity) {
                    object = Blocks.FIRE;
                } else if (entity instanceof LeashKnotEntity) {
                    object = Items.LEAD;
                } else if (entity instanceof FireballEntity) {
                    object = Items.FIRE_CHARGE;
                } else if (entity instanceof ItemFrameEntity) {
                    object = Items.ITEM_FRAME;
                } else if (entity instanceof DisplayEntity.ItemDisplayEntity) {
                    object = Items.ITEM_FRAME;
                } else if (entity instanceof HopperMinecartEntity) {
                    object = Items.HOPPER_MINECART;
                } else if (entity instanceof FireworkRocketEntity) {
                    object = Items.FIREWORK_ROCKET;
                } else if (entity instanceof FallingBlockEntity) {
                    object = Items.DIRT;
                } else if (entity instanceof EyeOfEnderEntity) {
                    object = Items.ENDER_EYE;
                } else if (entity instanceof ExperienceOrbEntity) {
                    object = Items.EXPERIENCE_BOTTLE;
                } else if (entity instanceof ExperienceBottleEntity) {
                    object = Items.EXPERIENCE_BOTTLE;
                } else if (entity instanceof EnderPearlEntity) {
                    object = Items.ENDER_PEARL;
                } else if (entity instanceof EndCrystalEntity) {
                    object = Items.END_CRYSTAL;
                } else if (entity instanceof EggEntity) {
                    object = Items.EGG;
                } else if (entity instanceof AreaEffectCloudEntity) {
                    object = StatusEffects.LEVITATION;
                } else if (entity instanceof BoatEntity) {
                    object = Items.OAK_BOAT;
                } else if (entity instanceof ArrowEntity) {
                    object = Items.ARROW;
                } else if (entity instanceof DragonFireballEntity) {
                    object = Items.FIRE_CHARGE;
                } else if (entity instanceof DisplayEntity.BlockDisplayEntity) {
                    object = Items.ITEM_FRAME;
                } else if (name.equals("Player")) {
                    object = Items.PLAYER_HEAD;
                } else {
                    object = entity;
                }

                add(new PickerItem(name, object, "", entity));
            }

            //Sort list
            list.sort(Comparator.comparing(item -> {
                Entity entity = (Entity)item.customObject;
                if (item.name.equals("Player")) {
                    return 1;
                } else if (EntityUtils.isPassive(entity) && !(entity instanceof ProjectileEntity) && !(entity instanceof ItemEntity)) {
                    return 2;
                } else if (EntityUtils.isHostileMob(entity)) {
                    return 3;
                } else if (entity instanceof ProjectileEntity) {
                    return 4;
                } else if (entity instanceof ItemEntity) {
                    return 5;
                }  else {
                    return 6;
                }
            }));
        }
    }

    public static boolean isValid(Entity entity, Setting setting) {
        List<PickerItem> selected = INSTANCE.getSelectedFromSetting(setting);
        for (PickerItem item : selected) {
            if (item.name.equals("Player") && entity instanceof PlayerEntity) {
                return true;
            } else if (item.name.equals("Item") && entity instanceof ItemEntity) {
                return true;
            } else if (item.name.equals(entity.getName().getString())) {
                return true;
            }
        }

        return false;
    }
}
