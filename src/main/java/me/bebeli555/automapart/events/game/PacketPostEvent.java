package me.bebeli555.automapart.events.game;

import me.bebeli555.automapart.events.Cancellable;
import net.minecraft.network.packet.Packet;

public class PacketPostEvent extends Cancellable {
	public Packet<?> packet;
	
	public PacketPostEvent(Packet<?> packet) {
		this.packet = packet;
	}
}
