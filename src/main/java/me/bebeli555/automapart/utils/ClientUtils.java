package me.bebeli555.automapart.utils;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.mods.ClientSettings;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ClientUtils extends Utils {
    /**
     * Sends a clientsided message with the given color and moduleName
     */
    public static void sendMessage(String text, Formatting color, String moduleName) {
        if (mc.player == null) {
            return;
        }

        //Send message
        String module = "";
        if (ClientSettings.clientMessageIncludeModule.bool() && !moduleName.isEmpty()) {
            module = " " + moduleName;
        }

        if (color == null) {
            color = Formatting.byName(ClientSettings.clientMessageColor.string());
        }

        Text textComponent = Text.literal(Formatting.byName(ClientSettings.clientMessageBracketsColor.string()) + "[" + Formatting.byName(ClientSettings.clientMessageNameColor.string()) + Mod.NAME + module + Formatting.byName(ClientSettings.clientMessageBracketsColor.string()) + "] " + color + text);
        mc.player.sendMessage(textComponent, false);
    }

    /**
     * Send clientsided message without moduleName
     */
    public static void sendMessage(String text, Formatting color) {
        sendMessage(text, color, "");
    }

    /**
     * Sends clientsided message without module name using default color set in config
     */
    public static void sendMessage(String text) {
        sendMessage(text, Formatting.byName(ClientSettings.clientMessageColor.string()));
    }

    /**
     * Sends a clientsided message with hover and click events
     */
    public static void sendMessage(List<String[]> list) {
        Formatting color = Formatting.byName(ClientSettings.clientMessageColor.string());
        Text textComponent = Text.literal("" + color);
        for (String[] s : list) {
            Text component = Text.literal(s[0]);
            component = component.getWithStyle(component.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(s[1])))).get(0);
            if (s.length > 2 ) component = component.getWithStyle(component.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, s[2]))).get(0);
            textComponent.getSiblings().add(component);
        }

        mc.player.sendMessage(textComponent);
    }
}
