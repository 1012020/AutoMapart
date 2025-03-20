package me.bebeli555.automapart.events.game;

import net.minecraft.item.ItemStack;

public class SectionVisibleEvent {
    public ItemStack.TooltipSection section;
    public boolean visible;

    public SectionVisibleEvent(ItemStack.TooltipSection section, boolean visible) {
        this.section = section;
        this.visible = visible;
    }
}
