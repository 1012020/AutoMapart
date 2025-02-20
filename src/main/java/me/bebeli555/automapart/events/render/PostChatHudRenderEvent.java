package me.bebeli555.automapart.events.render;

import me.bebeli555.automapart.events.Cancellable;
import net.minecraft.client.gui.DrawContext;

public class PostChatHudRenderEvent extends Cancellable {
    public DrawContext context;
    public int currentTick;

    public PostChatHudRenderEvent(DrawContext context, int currentTick) {
        this.context = context;
        this.currentTick = currentTick;
    }
}
