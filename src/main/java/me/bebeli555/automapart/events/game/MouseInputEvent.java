package me.bebeli555.automapart.events.game;

public class MouseInputEvent {
    private final int button, action;

    public MouseInputEvent(int button, int action) {
        this.button = button;
        this.action = action;
    }

    public int getButton() {
        return button;
    }

    public int getAction() {
        return action;
    }

    public static class Scroll {
        public double horizontal, vertical;
        public boolean up;

        public Scroll(double horizontal, double vertical) {
            this.horizontal = horizontal;
            this.vertical = vertical;

            this.up = vertical > 0;
        }
    }

    public static class MiddleClick {

    }
}
