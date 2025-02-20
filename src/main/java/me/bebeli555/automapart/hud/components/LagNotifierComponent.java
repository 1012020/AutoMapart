package me.bebeli555.automapart.hud.components;

import com.google.common.eventbus.Subscribe;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.game.PacketEvent;
import me.bebeli555.automapart.events.game.PacketServerEvent;
import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.hud.HudEditor;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;

public class LagNotifierComponent extends HudComponent {
    private static long prevTime, lastServerPacket;
    private static float[] ticks = new float[20];
    private static int currentTick;
	public static double lagSeconds;

	public static Setting lagNotifier = new Setting(Mode.BOOLEAN, "LagNotifier", true, "Shows when the server is not responding");

	public LagNotifierComponent() {
		super(HudCorner.TOP_LEFT, lagNotifier);
		Mod.EVENT_BUS.register(this);
		this.defaultX = 0;
		this.defaultY = 171;
	}
	
	@Override
	public void onRender(DrawContext context, float partialTicks) {
		lagSeconds = (double)Math.abs(System.currentTimeMillis() - lastServerPacket) / (double)1000;
		if (lastServerPacket != -1 && Math.abs(System.currentTimeMillis() - lastServerPacket) > 3500 && !mc.isInSingleplayer() || HudEditor.INSTANCE.isOn()) {
			String seconds = decimal((double)Math.abs(System.currentTimeMillis() - lastServerPacket) / (double)1000, 1);
			String text = g + "Server not responding " + w + seconds + "s";
			drawString(context, text, 0, 0, -1);
		}
	}

	public static double getTps() {
        int tickCount = 0;
        float tickRate = 0.0f;

		for (final float tick : ticks) {
			if (tick > 0.0f) {
				tickRate += tick;
				tickCount++;
			}
		}

        return MathHelper.clamp((tickRate / tickCount), 0.0f, 20.0f);
	}
	
	@Subscribe
	public void packetEvent(PacketEvent event) {
		if (event.packet instanceof WorldTimeUpdateS2CPacket) {
			if (prevTime != -1) {
				ticks[currentTick % ticks.length] = MathHelper.clamp((20.0f / ((float) (System.currentTimeMillis() - prevTime) / 1000.0f)), 0.0f, 20.0f);
				currentTick++;
			}

			prevTime = System.currentTimeMillis();
		}
	}
	
	@Subscribe
	public void packetServerEvent(PacketServerEvent event) {
		lastServerPacket = System.currentTimeMillis();
	}
}
