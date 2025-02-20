package me.bebeli555.automapart.hud.components;

import com.google.common.eventbus.Subscribe;
import io.netty.buffer.Unpooled;
import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.game.PacketPostEvent;
import me.bebeli555.automapart.gui.Gui;
import me.bebeli555.automapart.hud.HudComponent;
import me.bebeli555.automapart.utils.globalsettings.GlobalBorderSettings;
import me.bebeli555.automapart.settings.Mode;
import me.bebeli555.automapart.settings.Setting;
import me.bebeli555.automapart.settings.SettingList;
import me.bebeli555.automapart.settings.SettingValue;
import me.bebeli555.automapart.utils.objects.Timer;
import me.bebeli555.automapart.utils.font.ColorHolder;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class TrafficComponent extends HudComponent {
    //CPacket, Amount, PacketSize, ms
    public List<Object[]> list = new ArrayList<>();
    public String lastSent, lastReceived;
    public String sentString, receivedString;
    public Timer timer = new Timer();
    public Timer sentTimer = new Timer();
    public Timer receivedTimer = new Timer();

    public static Setting traffic = new Setting(Mode.BOOLEAN, "Traffic" , false, "Shows packets and traffic data");
        public static Setting scaleSetting = new Setting(traffic, Mode.DOUBLE, "Scale" , new SettingValue(1, 0.3, 3, 0.1), "Scale for the entire thing");
        public static Setting gapSetting = new Setting(traffic, Mode.INTEGER, "Gap", new SettingValue(9, 3, 25, 1), "Gap between the texts");
        public static Setting nameColor = new Setting(traffic, Mode.COLOR, "NameColor", -3618616, "Color of the name before the value");
        public static Setting valueColor = new Setting(traffic, Mode.COLOR, "ValueColor", -1900532, "Color of the value text");
        public static Setting background = new Setting(traffic, Mode.COLOR, "Background", 838860800, "Background color");
        public static SettingList border = GlobalBorderSettings.get(traffic, false, true, -16777216);
        public static Setting onlyRecent = new Setting(traffic, Mode.BOOLEAN, "OnlyRecent", false, "When toggled shows only packets from the past", "Time that is specified below");
            public static Setting onlyRecentSeconds = new Setting(onlyRecent, Mode.INTEGER, "Seconds", new SettingValue(5, 1, 60, 1));
        public static Setting lastPackets = new Setting(traffic, Mode.BOOLEAN, "LastPackets", true, "Shows the last sent and received packets");
            public static Setting lastUpdate = new Setting(lastPackets, Mode.DOUBLE, "LastUpdate", new SettingValue(1.5, 0, 10, 0.1), "How often to update the last sent/received field", "If this is too low its very spammy and u cant see it");

    public TrafficComponent() {
        super(HudCorner.TOP_LEFT, traffic);
        this.defaultX = 205;
        this.defaultY = 8;
        Mod.EVENT_BUS.register(this);
    }

    @Override
    public void onRender(DrawContext context, float partialTicks) {
        MatrixStack stack = context.getMatrices();

        float scale = scaleSetting.asFloat();
        float gap = gapSetting.asInt() * scale;
        ColorHolder c1 = new ColorHolder(nameColor.asInt());
        ColorHolder c2 = new ColorHolder(valueColor.asInt());

        stack.push();
        stack.scale(scale, scale, scale);

        //Calculate data
        if (timer.hasPassed(50)) {
            timer.reset();

            new Thread(() -> {
                long sent = 0;
                long received = 0;
                long sentBytes = 0;
                long receivedBytes = 0;

                List<Object[]> reformat = new ArrayList<>();
                List<Object[]> copy = new ArrayList<>(list);
                for (Object[] o : copy) {
                    if (o == null) {
                        continue;
                    }

                    long passed = Math.abs(Long.parseLong(String.valueOf(o[3])) - System.currentTimeMillis());
                    if (passed > onlyRecentSeconds.asInt() * 1000L && onlyRecent.bool()) {
                        continue;
                    }

                    if (passed > 120 * 1000) {
                        reformat.add(o);
                    }

                    if ((boolean)o[0]) {
                        sent += (long)o[1];
                        sentBytes += (long)o[2];
                    } else {
                        received += (long)o[1];
                        receivedBytes += (long)o[2];
                    }
                }

                //Create display strings
                sentString = sent + " (" + decimal(((double)sentBytes / 1000000), 2) + "MB)";
                receivedString = received + " (" + decimal(((double)receivedBytes / 1000000), 2) + "MB)";

                //Reformat list, delete old data so no memory leak
                sent = received = sentBytes = receivedBytes = 0;
                for (Object[] o : reformat) {
                    list.remove(o);

                    if ((boolean)o[0]) {
                        sent += (long)o[1];
                        sentBytes += (long)o[2];
                    } else {
                        received += (long)o[1];
                        receivedBytes += (long)o[2];
                    }
                }

                list.add(new Object[]{true, sent, sentBytes, System.currentTimeMillis() - 500 * 1000});
                list.add(new Object[]{false, received, receivedBytes, System.currentTimeMillis() - 500 * 1000});
            }).start();
        }

        List<String> list = new ArrayList<>();
        list.add(c1 + "Sent packets " + c2 + sentString);
        list.add(c1 + "Received packets " + c2 + receivedString);
        if (lastPackets.bool()) {
            list.add(c1 + "Last sent " + c2 + lastSent);
            list.add(c1 + "Last received " + c2 + lastReceived);
        }

        HudPoint point = new HudPoint((getxAdd() - add(scale)) / scale, (getyAdd() - add(scale)) / scale, (getxAdd() + getLongestString(list, scale) + add(scale)) / scale, (getyAdd() + list.size() * gap) / scale);
        this.renderedPoints.add(new HudPoint(point.x * scale, point.y * scale, point.x2 * scale, point.y2 * scale));

        //Render background
        Gui.drawRect(stack, point.x, point.y, point.x2, point.y2, background.asInt());

        //Render border
        GlobalBorderSettings.render(border, stack, point.x, point.y, point.x2, point.y2);

        //Draw strings
        for (int i = 0; i < list.size(); i++) {
            Gui.fontRenderer.drawString(stack, list.get(i), (float)(getxAdd() / scale), (float)((getyAdd() + (i * gap)) / scale), -1);
        }

        stack.pop();
    }

    @Subscribe
    private void onPacketPost(PacketPostEvent event) {
        if (shouldRender()) {
            String name = "Unknown packet";
            if (name.contains("C2S")) {
                if (sentTimer.hasPassed((int)(lastUpdate.asDouble() * 1000))) {
                    sentTimer.reset();
                    lastSent = name.replace("C2SPacket", "");
                }

                list.add(new Object[]{true, (long)1, (long)getPacketSize(event.packet), System.currentTimeMillis()});
            } else {
                if (receivedTimer.hasPassed((int)(lastUpdate.asDouble() * 1000))) {
                    receivedTimer.reset();
                    lastReceived = name.replace("S2CPacket", "");
                }

                list.add(new Object[]{false, (long)1, (long)getPacketSize(event.packet), System.currentTimeMillis()});
            }
        }
    }

    public int getPacketSize(Packet<?> packet) {
        try {
            PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
            packet.write(buffer);

            return buffer.capacity();
        } catch (Exception e) {
            return 0;
        }
    }
}
