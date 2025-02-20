package me.bebeli555.automapart.events.render;

import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

public class RenderEntityTextName {
    private final Entity entity;
    private Text name;

    public RenderEntityTextName(Entity entity, Text name) {
        this.entity = entity;
        this.name = name;
    }

    public Entity getEntity() {
        return entity;
    }

    public Text getDisplayName() {
        return name;
    }

    public void setDisplayName(Text name) {
        this.name = name;
    }
}
