package me.bebeli555.automapart.events.render;

import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.text.Text;

import java.util.Collection;

public class RenderBossBarEvent {
    public static class Iterator {
        public Collection<ClientBossBar> collection;

        public Iterator(Collection<ClientBossBar> collection) {
            this.collection = collection;
        }
    }

    public static class Name {
        public ClientBossBar bar;
        public Text name;

        public Name(ClientBossBar bar) {
            this.bar = bar;
            this.name = bar.getName();
        }
    }
}
