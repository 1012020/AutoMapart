package me.bebeli555.automapart.hud.components;

import com.google.common.eventbus.Subscribe;
import io.netty.buffer.Unpooled;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.game.ClientTickEvent;
import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.hud.HudRenderer;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.SettingValue;
import me.bebeli555.automapart.utils.BlockUtils;
import me.bebeli555.automapart.utils.EntityUtils;
import me.bebeli555.automapart.utils.ItemUtils;
import me.bebeli555.automapart.utils.objects.ClientStatus;
import me.bebeli555.automapart.utils.objects.Timer;
import me.bebeli555.automapart.utils.font.ColorHolder;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class InfoClusterComponent extends HudComponent {
	public static InfoClusterComponent INSTANCE;
	public List<Object[]> renderList = new ArrayList<>();
	public boolean shadow = true;

	public Timer memoryTimer = new Timer();
	public String memoryText = "";

	public long[] queueData = new long[]{-1, -1, System.currentTimeMillis()};
	public Timer queueTimer = new Timer();

	public long lastOffline = System.currentTimeMillis();

	public Timer chunkTimer = new Timer();
	public int chunkSize;

	public static Setting infoCluster = new Setting(Mode.BOOLEAN, "InfoCluster", true, "Components for the info cluster", "Thats in the top right by default");
		public static Setting freeMove = new Setting(infoCluster, Mode.BOOLEAN, "FreeMove", false, "Move each text to anywhere on the screen", "Instead of moving the entire cluster");
			public static Setting disableIndexing = new Setting(freeMove, Mode.BOOLEAN, "DisableIndexing", false, "Disables indexing where they move up and down", "based on the other texts");
		public static Setting scaleSetting = new Setting(infoCluster, Mode.DOUBLE, "Scale", new SettingValue(1, 0.3, 3, 0.1), "Scale for all the texts");
		public static Setting gapSetting = new Setting(infoCluster, Mode.INTEGER, "Gap", new SettingValue(8, 3, 25, 1), "Gap between the texts");
		public static Setting background = new Setting(infoCluster, Mode.COLOR, "Background", 0, "Background color for all the texts");
		public static Setting nameColor = new Setting(infoCluster, Mode.COLOR, "NameColor", -5592406, "Color of the name, AKA primary color");
		public static Setting valueColor = new Setting(infoCluster, Mode.COLOR, "ValueColor", -1, "Color of the value, or secondary color");
		public static Setting tps = new Setting(infoCluster, Mode.BOOLEAN, "TPS", true, "Shows server tps");
			public static Setting tpsIndex = new Setting(tps, Mode.INTEGER, "Index", new SettingValue(0, -15, 15, 1), "Index on the cluster where its rendered");
		public static Setting fps = new Setting(infoCluster, Mode.BOOLEAN, "FPS", true, "Shows ur fps");
			public static Setting fpsShowMin = new Setting(fps, Mode.BOOLEAN, "ShowMin", false, "Shows min fps after the normal fps");
			public static Setting fpsIndex = new Setting(fps, Mode.INTEGER, "Index", new SettingValue(0, -15, 15, 1), "Index on the cluster where its rendered");
		public static Setting speed = new Setting(infoCluster, Mode.BOOLEAN, "Speed", true, "Shows ur speed in blocks per second");
			public static Setting speedIndex = new Setting(speed, Mode.INTEGER, "Index", new SettingValue(0, -15, 15, 1), "Index on the cluster where its rendered");
			public static Setting speedUseBPS = new Setting(speed, Mode.BOOLEAN, "UseBPS", false, "Uses Blocks per second instead of km/h");
		public static Setting ping = new Setting(infoCluster, Mode.BOOLEAN, "Ping", true, "Shows ur ping");
			public static Setting pingIndex = new Setting(ping, Mode.INTEGER, "Index", new SettingValue(0, -15, 15, 1), "Index on the cluster where its rendered");
		public static Setting durability = new Setting(infoCluster, Mode.BOOLEAN, "Durability", true, "Shows durability for ur item");
			public static Setting durabilityIndex = new Setting(durability, Mode.INTEGER, "Index", new SettingValue(0, -15, 15, 1), "Index on the cluster where its rendered");
		public static Setting potions = new Setting(infoCluster, Mode.BOOLEAN, "Potions", true, "Shows potion effects and doesnt render the vanilla hud overlays");
			public static Setting potionsIndex = new Setting(potions, Mode.INTEGER, "Index", new SettingValue(0, -15, 15, 1), "Index on the cluster where its rendered");
		public static Setting statusSetting = new Setting(infoCluster, Mode.BOOLEAN, "Status", true, "Renders the status of some modules like Elytrabot/AutoBuilder");
			public static Setting statusIndex = new Setting(statusSetting, Mode.INTEGER, "Index", new SettingValue(0, -15, 15, 1), "Index on the cluster where its rendered");
			public static Setting chunkSizeSetting = new Setting(infoCluster, Mode.BOOLEAN, "ChunkSize", false, "Shows the current chunk size");
			public static Setting chunkSizeIndex = new Setting(chunkSizeSetting, Mode.INTEGER, "Index", new SettingValue(0, -15, 15, 1), "Index on the cluster where its rendered");
		public static Setting memoryUsage = new Setting(infoCluster, Mode.BOOLEAN, "Memory", false, "Shows the JVM memory usage");
			public static Setting memoryUsageIndex = new Setting(memoryUsage, Mode.INTEGER, "Index", new SettingValue(0, -15, 15, 1), "Index on the cluster where its rendered");
			public static Setting memoryUsageShowMax = new Setting(memoryUsage, Mode.BOOLEAN, "ShowMax", false, "Shows the max memory allowed");
		public static Setting serverBrand = new Setting(infoCluster, Mode.BOOLEAN, "ServerBrand", false, "Shows the current server brand");
			public static Setting serverBrandIndex = new Setting(serverBrand, Mode.INTEGER, "Index", new SettingValue(0, -15, 15, 1), "Index on the cluster where its rendered");
			public static Setting serverBrandIp = new Setting(serverBrand, Mode.BOOLEAN, "ShowIP", false, "Shows the IP too");
		public static Setting rotation = new Setting(infoCluster, Mode.BOOLEAN, "Rotation", false, "Shows yaw and pitch");
			public static Setting rotationIndex = new Setting(rotation, Mode.INTEGER, "Index", new SettingValue(0, -15, 15, 1), "Index on the cluster where its rendered");
		public static Setting onlineTime = new Setting(infoCluster, Mode.BOOLEAN, "OnlineTime", false, "Shows how long you have been online for");
			public static Setting onlineTimeIndex = new Setting(onlineTime, Mode.INTEGER, "Index", new SettingValue(0, -15, 15, 1), "Index on the cluster where its rendered");
			public static Setting onlineTimeShowSeconds = new Setting(onlineTime, Mode.BOOLEAN, "ShowSeconds", true);
			public static Setting onlineTimeReset = new Setting(onlineTime, Mode.BOOLEAN, "Reset", true, "Resets on server change");
		public static Setting queue2b2t = new Setting(infoCluster, Mode.BOOLEAN, "2b2tQueue", false, "Shows current 2b2t queue: Priority/Normal", "Note: this uses 2bqueue.info service provided by tycrek for the data", "So once turned on it will make requests to that server every now and then");
			public static Setting queue2b2tIndex = new Setting(queue2b2t, Mode.INTEGER, "Index", new SettingValue(0, -15, 15, 1), "Index on the cluster where its rendered");
			public static Setting queue2b2tShowLastUpdate = new Setting(queue2b2t, Mode.BOOLEAN, "UpdatedAgo", false, "Shows how long ago it was updated");
		public static Setting playerCount = new Setting(infoCluster, Mode.BOOLEAN, "PlayerCount", false, "Shows current server player count");
			public static Setting playerCountIndex = new Setting(playerCount, Mode.INTEGER, "Index", new SettingValue(0, -15, 15, 1), "Index on the cluster where its rendered");
		public static Setting saturation = new Setting(infoCluster, Mode.BOOLEAN, "Saturation", false, "Shows your saturation level");
			public static Setting saturationIndex = new Setting(saturation, Mode.INTEGER, "Index", new SettingValue(0, -15, 15, 1), "Index on the cluster where its rendered");
		public static Setting pcTime = new Setting(infoCluster, Mode.BOOLEAN, "PcTime", false, "Shows your local computer time");
			public static Setting pcTimeIndex = new Setting(pcTime, Mode.INTEGER, "Index", new SettingValue(0, -15, 15, 1), "Index on the cluster where its rendered");
			public static Setting pcTimeFormat = new Setting(pcTime, Mode.TEXT, "Format", "dd.MM.yyyy HH:mm", "What date format to use, this uses java's SimpleDateFormat", "So google it if you want to know how to modify it");
		public static Setting biome = new Setting(infoCluster, Mode.BOOLEAN, "Biome", false, "Shows current biome");
			public static Setting biomeIndex = new Setting(biome, Mode.INTEGER, "Index", new SettingValue(0, -15, 15, 1), "Index on the cluster where its rendered");
		public static Setting lookingAt = new Setting(infoCluster, Mode.BOOLEAN, "LookingAt", false, "Shows block/entity name you are looking at");
			public static Setting lookingAtIndex = new Setting(lookingAt, Mode.INTEGER, "Index", new SettingValue(0, -15, 15, 1), "Index on the cluster where its rendered");
			public static Setting lookingAtDontShowAir = new Setting(lookingAt, Mode.BOOLEAN, "DontShowAir", true, "Disables this from the cluster when the object is Air");

	public InfoClusterComponent() {
		super(HudCorner.TOP_RIGHT, infoCluster);
		INSTANCE = this;
		Mod.EVENT_BUS.register(this);
	}

	@Subscribe
	public void tickEvent(ClientTickEvent e) {
		if (!onlineTimeReset.bool()) {
			return;
		}

		if (mc.currentScreen instanceof ConnectScreen || mc.currentScreen instanceof SelectWorldScreen || mc.currentScreen instanceof TitleScreen) {
			lastOffline = System.currentTimeMillis();
		}
	}

	@Override
	public void onRender(DrawContext context, float partialTicks) {
		MatrixStack stack = context.getMatrices();

		fetchedTextAdds.clear();

		ColorHolder p1 = new ColorHolder(nameColor.asInt());
		ColorHolder s2 = new ColorHolder(valueColor.asInt());

		float scale = (float) scaleSetting.asDouble();
		stack.push();
		stack.scale(scale, scale, scale);

		//Render status
		if (statusSetting.bool() && !ClientStatus.status.isEmpty()) {
			for (String s : ClientStatus.status) {
				if (s == null) {
					break;
				}

				renderInfo(p1 + "Status " + s, statusIndex.asInt());
			}
		} else if (!statusSetting.bool()) {
			textAdds.remove("Status");
		}

		//Render potions
		if (potions.bool()) {
			shadow = false;
			for (StatusEffectInstance effect : mc.player.getStatusEffects()) {
				String text = effect.getEffectType().getName().getString();
				if (effect.getAmplifier() > 0) {
					text += " " + (effect.getAmplifier() + 1);
				}

				text += " " + w + StatusEffectUtil.getDurationText(effect, 1, 20).getString();
				ColorHolder color = new ColorHolder(effect.getEffectType().getColor());
				color.a = 255;

				renderInfo(color + text, potionsIndex.asInt());
			}
			shadow = true;
		}

		//Render online time
		if (onlineTime.bool()) {
			renderInfo(p1 + "Online for" + s2 + getTimeAgoString(lastOffline, onlineTimeShowSeconds.bool()), onlineTimeIndex.asInt());
		}

		//Render pc time
		if (pcTime.bool()) {
			try {
				DateFormat dateFormat = new SimpleDateFormat(pcTimeFormat.string());
				renderInfo(p1 + "Time " + s2 + dateFormat.format(new Date()), pcTimeIndex.asInt());
			} catch (Exception e) {
				renderInfo(p1 + "Time " + s2 + "Error! you fucked up the format :(", pcTimeIndex.asInt());
			}
		}

		//Render rotation
		if (rotation.bool()) {
			renderInfo(p1 + "Yaw " + s2 + decimal(MathHelper.wrapDegrees(mc.player.getYaw()), 2) + p1 + " Pitch " + s2 + decimal(MathHelper.wrapDegrees(mc.player.getPitch()), 2), rotationIndex.asInt());
		}

		//Render chunksize
		if (chunkSizeSetting.bool()) {
			if (chunkTimer.hasPassed(150)) {
				chunkTimer.reset();

				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
				ChunkDataS2CPacket packetData = new ChunkDataS2CPacket(mc.player.getWorld().getChunk(mc.player.getChunkPos().x, mc.player.getChunkPos().z), mc.player.getWorld().getLightingProvider(), null, null);
				packetData.write(buf);
				chunkSize = buf.readableBytes();
			}

			renderInfo(p1 + "ChunkSize " + s2 + decimal((double)chunkSize / 1000, 2) + "KB", chunkSizeIndex.asInt());
		}

		//Render memory
		if (memoryUsage.bool()) {
			if (memoryTimer.hasPassed(50)) {
				memoryTimer.reset();
				long memory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L;
				long max = Runtime.getRuntime().maxMemory() / 1048576L;
				memoryText = p1 + "Memory " + s2 + memory + (memoryUsageShowMax.bool() ? "/" + max : "") + "MB";
			}

			renderInfo(memoryText, memoryUsageIndex.asInt());
		}

		//Render server brand
		if (serverBrand.bool()) {
			String serverBrand = "unknown";
			if (serverBrandIp.bool()) {
				//TODO: serverBrand += ", " + InformationUtil.lastIp;
			}

			renderInfo(p1 + "Server " + s2 + serverBrand, serverBrandIndex.asInt());
		}

		//Render queue
		if (queue2b2t.bool()) {
			if (queueTimer.hasPassed(60000)) {
				queueTimer.reset();

				//Fetch queue data
				new Thread(() -> {
					try {
						String html = getHtml("https://2bqueue.info/queue");
						int prio = Integer.parseInt(html.split("\"prio\":")[1].split(",")[0]);
						int regular = Integer.parseInt(html.split("\"regular\":")[1].split(",")[0]);
						long ms = Long.parseLong(html.split("\"timems\":\"")[1].split("\",")[0]);

						queueData = new long[]{prio, regular, ms};
					} catch (Exception e) {
						//e.printStackTrace();
					}
				}).start();
			}

			String updateAgo = "";
			if (queue2b2tShowLastUpdate.bool()) {
				updateAgo = getTimeAgoString(queueData[2], true);
				updateAgo += " ago";
			}

			renderInfo(p1 + "Queue " + s2 + queueData[0] + "/" + queueData[1] + updateAgo, queue2b2tIndex.asInt());
		}

		//Render playercount
		if (playerCount.bool()) {
			renderInfo(p1 + "Players " + s2 + mc.player.networkHandler.getListedPlayerListEntries().size(), playerCountIndex.asInt());
		}

		//Render biome
		if (biome.bool()) {
			renderInfo(p1 + "Biome " + s2 + mc.player.getWorld().getBiome(mc.player.getBlockPos()).getKey().get().toString().replace("ResourceKey[minecraft:worldgen/biome / minecraft:", "").replace("]", ""), biomeIndex.asInt());
		}

		//Render looking at
		if (lookingAt.bool()) {
			String name = null;
			if (mc.crosshairTarget instanceof EntityHitResult result) {
				name = result.getEntity().getName().getString();
			} else if (mc.crosshairTarget instanceof BlockHitResult result) {
				name = BlockUtils.getBlock(result.getBlockPos()).getName().getString();
			}

			if (name != null) {
				if (!lookingAtDontShowAir.bool() || !name.equals("Air")) {
					renderInfo(p1 + "LookingAt " + s2 + name, lookingAtIndex.asInt());
				}
			}
		}

		//Render saturation
		if (saturation.bool()) {
			renderInfo(p1 + "Saturation " + s2 + decimal(mc.player.getHungerManager().getSaturationLevel(), 2), saturationIndex.asInt());
		}

		//Render speed
		if (speed.bool()) {
			String speed = decimal(EntityUtils.getSpeed(mc.player) * 71.35, 1) + "km/h";
			if (speedUseBPS.bool()) {
				speed = decimal(EntityUtils.getSpeed(mc.player) * 20.6, 2) + "b/s";
			}

			renderInfo(p1 + "Speed " + s2 + speed, speedIndex.asInt());
		}
		
		//Render Ping
		if (ping.bool()) {
			try {
				renderInfo(p1 + "Ping " + s2 + mc.player.networkHandler.getPlayerListEntry(mc.cameraEntity.getUuid()).getLatency() + "ms", pingIndex.asInt());
			} catch (NullPointerException ignored) {}
		} else {
			textAdds.remove("Ping");
		}
		
		//Render durability
		if (durability.bool()) {
			ItemStack itemStack = mc.player.getActiveItem();
			if (ItemUtils.hasDurability(itemStack)) {
				renderInfo(p1 + "Durability " + ItemUtils.getDurabilityColor(itemStack) + ItemUtils.getDurability(itemStack), durabilityIndex.asInt());
			}
		} else {
			textAdds.remove("Durability");
		}
		
		//Render TPS
		if (tps.bool()) {
			String decimal = decimal(LagNotifierComponent.getTps(), 2);
			renderInfo(p1 + "TPS " + s2 + decimal, tpsIndex.asInt());
		}
		
		//Render FPS
		if (fps.bool()) {
			String fps = "" + mc.getCurrentFps();
			if (fpsShowMin.bool()) {
				fps += " (" + (int)HudRenderer.minFPS + ")";
			}

			renderInfo(p1 + "FPS " + s2 + fps, fpsIndex.asInt());
		}

		//Do render
		doRender(context);

		//Remove toggled off text adds
		List<String> copy = new ArrayList<>(textAdds.keySet());
		for (String key : copy) {
			if (key == null) {
				continue;
			}

			if (!fetchedTextAdds.contains(key)) {
				if (key.startsWith("Ping") || key.startsWith("Durability") || key.startsWith("Status")) {
					continue;
				}

				textAdds.remove(key);
			}
		}

		stack.pop();
	}

	public static String getTimeAgoString(long ms, boolean showSeconds) {
		long differenceInMilliSeconds = Math.abs(System.currentTimeMillis() - ms);
		long differenceInHours = (differenceInMilliSeconds / (60 * 60 * 1000)) % 24;
		long differenceInMinutes = (differenceInMilliSeconds / (60 * 1000)) % 60;
		long differenceInSeconds = (differenceInMilliSeconds / 1000) % 60;

		String hours = differenceInHours > 0 ? (differenceInHours == 1 ? " 1 hour" : " " + differenceInHours + " hours") : "";
		String minutes = differenceInMinutes > 0 ? (differenceInMinutes == 1 ? " 1 min" : " " + differenceInMinutes + " mins") : "";
		String seconds = differenceInSeconds > 0 ? (differenceInSeconds == 1 ? " 1 sec" : " " + differenceInSeconds + " secs") : "";
		if (!showSeconds) {
			seconds = "";
		}

		return hours + minutes + seconds;
	}

	public void doRender(DrawContext context) {
		for (int i = 0; i < renderList.size(); i++) {
			int index = (int)renderList.get(i)[1];
			renderList.get(i)[1] = (i * 2) + index;
		}

		renderList.sort(Comparator.comparingInt(o -> (int)o[1]));
		int amount = 0;
		double scale = scaleSetting.asDouble();
		float add = (gapSetting.asInt() * (float) scaleSetting.asDouble()) / (float)scale;

		for (Object[] object : renderList) {
			if (corner == HudCorner.TOP_LEFT || corner == HudCorner.TOP_RIGHT) {
				drawString(context, (String)object[0], 0, amount * add, -1, scale);
			} else {
				drawString(context, (String)object[0], 0, -(amount * add), -1, scale);
			}

			if (!disableIndexing.bool()) {
				amount++;
			}
		}

		renderList.clear();
	}

	public void renderInfo(String text, int index) {
		renderList.add(new Object[]{text, index, shadow});
	}
}
 