package me.bebeli555.automapart.utils.input;

import com.google.common.eventbus.Subscribe;
import me.bebeli555.automapart.events.game.MouseInputEvent;

import java.util.ArrayList;
import java.util.List;

public class Mouse {
    public static List<IntButton> down = new ArrayList<>();

    @Subscribe
    private void onMouse(MouseInputEvent e) {
        if (e.getAction() == 1) {
            if (!down.contains(new IntButton(e.getButton()))) {
                down.add(new IntButton(e.getButton()));
            }
        } else if (e.getAction() == 0) {
            down.remove(new IntButton(e.getButton()));
        }
    }

    public static boolean isButtonDown(int button) {
        return down.contains(new IntButton(button));
    }

    public static class IntButton {
        public int button;

        public IntButton(int button) {
            this.button = button;
        }

        @Override
        public boolean equals(Object other) {
            IntButton o = (IntButton)other;
            return o.button == this.button;
        }
    }
}
