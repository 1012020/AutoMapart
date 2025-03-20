package me.bebeli555.automapart.events.render;

public class SetTooltipSizeEvent {
    public int x, y, width, height;

    public SetTooltipSizeEvent(int x, int y, int width, int height) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
    }
}
