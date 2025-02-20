package me.bebeli555.automapart.events.game;

import me.bebeli555.automapart.events.Cancellable;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;

public class ChatMessageReceivedEvent extends Cancellable {
    public Text message;
    public MessageSignatureData signature;
    public int ticks;
    public MessageIndicator indicator;
    public boolean refresh;

    public ChatMessageReceivedEvent(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh) {
        this.message = message;
        this.signature = signature;
        this.ticks = ticks;
        this.indicator = indicator;
        this.refresh = refresh;
    }
}
