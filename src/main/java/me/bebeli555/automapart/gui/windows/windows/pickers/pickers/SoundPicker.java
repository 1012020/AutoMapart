package me.bebeli555.automapart.gui.windows.windows.pickers.pickers;

import me.bebeli555.automapart.gui.windows.windows.pickers.PickerWindow;
import me.bebeli555.automapart.settings.Setting;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class SoundPicker extends PickerWindow {
    public static SoundPicker INSTANCE;

    public SoundPicker() {
        super("SoundPicker");
        INSTANCE = this;
        this.allowOnlyOneSelected = true;

        //Initialize items
        for (SoundEvent sound : Registries.SOUND_EVENT) {
            String temp = sound.getId().toString();
            if (temp.contains(".")) {
                temp = temp.split("\\.", 2)[1];
            }

            String name = StringUtils.capitalize(temp.replace(".", "-").replace("_", " "));
            add(new PickerItem(name, getItemForSound(name), "", sound));
        }
    }

    public static void playSound(Setting setting) {
        List<PickerItem> selected = INSTANCE.getSelectedFromSetting(setting);
        if (!selected.isEmpty()) {
            mc.world.playSound(mc.getCameraEntity(), mc.getCameraEntity().getBlockPos(), (SoundEvent)selected.get(0).customObject, SoundCategory.AMBIENT, 10222.5f, 1.5f);
        }
    }

    public Item getItemForSound(String name) {
        name = name.toLowerCase();
        if (name.contains("allay")) {
            return Items.ALLAY_SPAWN_EGG;
        } else if (name.contains("experience")) {
            return Items.EXPERIENCE_BOTTLE;
        } else if (name.contains("cave")) {
            return Items.TORCH;
        } else if (name.contains("basalt")) {
            return Items.BASALT;
        } else if (name.contains("crimson")) {
            return Items.CRIMSON_ROOTS;
        } else if (name.contains("nether")) {
            return Items.NETHERRACK;
        } else if (name.contains("soul sand")) {
            return Items.SOUL_SAND;
        } else if (name.contains("warped")) {
            return Items.WARPED_ROOTS;
        } else if (name.contains("underwater")) {
            return Items.WATER_BUCKET;
        } else if (name.contains("amethyst")) {
            return Items.AMETHYST_BLOCK;
        } else if (name.contains("ancient debris")) {
            return Items.ANCIENT_DEBRIS;
        } else if (name.contains("anvil")) {
            return Items.ANVIL;
        } else if (name.contains("armor")) {
            return Items.CHAINMAIL_CHESTPLATE;
        } else if (name.contains("arrow")) {
            return Items.ARROW;
        } else if (name.contains("axe")) {
            return Items.GOLDEN_AXE;
        } else if (name.contains("axolotl")) {
            return Items.AXOLOTL_BUCKET;
        } else if (name.contains("azalea")) {
            return Items.AZALEA;
        } else if (name.contains("bamboo")) {
            return Items.BAMBOO;
        } else if (name.contains("barrel")) {
            return Items.BARREL;
        } else if (name.contains("bat")) {
            return Items.BAT_SPAWN_EGG;
        } else if (name.contains("beacon")) {
            return Items.BEACON;
        } else if (name.contains("bee")) {
            return Items.BEE_NEST;
        } else if (name.contains("bell")) {
            return Items.BELL;
        } else if (name.contains("big dripleaf")) {
            return Items.BIG_DRIPLEAF;
        } else if (name.contains("blaze")) {
            return Items.BLAZE_ROD;
        } else if (name.contains("boat")) {
            return Items.OAK_BOAT;
        } else if (name.contains("bone")) {
            return Items.BONE;
        } else if (name.contains("bookshelf")) {
            return Items.BOOKSHELF;
        } else if (name.contains("book")) {
            return Items.BOOK;
        } else if (name.contains("furnace")) {
            return Items.FURNACE;
        } else if (name.contains("bottle")) {
            return Items.GLASS_BOTTLE;
        } else if (name.contains("brew")) {
            return Items.BREWING_STAND;
        } else if (name.contains("brush")) {
            return Items.BRUSH;
        } else if (name.contains("bubble")) {
            return Items.BUBBLE_CORAL;
        } else if (name.contains("lava")) {
            return Items.LAVA_BUCKET;
        } else if (name.contains("water")) {
            return Items.WATER_BUCKET;
        } else if (name.contains("bucket")) {
            return Items.BUCKET;
        } else if (name.contains("bundle")) {
            return Items.BUNDLE;
        } else if (name.contains("cake")) {
            return Items.CAKE;
        } else if (name.contains("camel")) {
            return Items.CAMEL_SPAWN_EGG;
        } else if (name.contains("cat")) {
            return Items.CAT_SPAWN_EGG;
        } else if (name.contains("chicken")) {
            return Items.CHICKEN;
        } else if (name.contains("coral")) {
            return Items.BUBBLE_CORAL;
        } else if (name.contains("fire")) {
            return Items.FLINT_AND_STEEL;
        } else if (name.contains("froglight")) {
            return Items.FROG_SPAWN_EGG;
        } else if (name.contains("leash")) {
            return Items.LEAD;
        }

        Item closestItem = Items.AIR;
        int closestDistance = Integer.MAX_VALUE;

        for (Item item : Registries.ITEM) {
            String itemName = item.getName().getString().toLowerCase().replace("spawn egg", "");
            if (name.contains("-")) {
                name = name.split("-")[0];
            }

            int distance = calculateStringDistance(name.toLowerCase(), itemName);
            if (distance < closestDistance) {
                closestDistance = distance;
                closestItem = item;
            }
        }

        return closestItem;
    }

    //Calculate the Levenshtein distance between two strings
    public int calculateStringDistance(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= str2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }

        return dp[str1.length()][str2.length()];
    }
}
