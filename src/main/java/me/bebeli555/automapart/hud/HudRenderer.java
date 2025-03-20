package me.bebeli555.automapart.hud;

import com.google.common.eventbus.Subscribe;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.render.RenderGuiEvent;
import me.bebeli555.automapart.mods.ClientSettings;
import me.bebeli555.automapart.utils.objects.Timer;

public class HudRenderer extends Mod {
    public static double minFPS;
    private double tempMinFPS;
    private long lastTime;
    private final Timer minFPSTimer = new Timer();

    @Subscribe
    private void renderGui(RenderGuiEvent e) {
        //Calculate min fps for info cluster component
        long currentTime = System.nanoTime();
        double frameTime = (currentTime - lastTime) / 1_000_000_000.0; // Convert nanoseconds to seconds
        double fps = 1.0 / frameTime;
        lastTime = currentTime;
        if (fps < tempMinFPS || minFPSTimer.hasPassed(1000)) {
            tempMinFPS = fps;

            if (minFPSTimer.hasPassed(1000)) {
                minFPS = tempMinFPS;
            }

            minFPSTimer.reset();
        }

        if (mc.player == null) {
            return;
        }

        //Render HUD. Design inspired by Future client
        if (!HudEditor.INSTANCE.isOn()) {
            if (!ClientSettings.hud.bool()) return;

            e.getMatrixStack().push();
            float scale = HudComponent.currentScale();
            e.getMatrixStack().scale(scale, scale, scale);
            HudComponent.renderedScale = scale;

            //Rainbow
            HudComponent.rainbow.factor = 0;
            HudComponent.rainbow.setSpeed(ClientSettings.hudRainbowSpeed.asInt());
            HudComponent.rainbow.onUpdate();

            for (HudComponent component : HudComponent.components) {
                if (component.shouldRender()) {
                    component.renderedPoints.clear();
                    component.onRender(e.context(), e.tickDelta());
                }
            }

            e.getMatrixStack().pop();
        }
    }
}
