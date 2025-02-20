package me.bebeli555.automapart.events.game;

import me.bebeli555.automapart.events.Cancellable;
import net.minecraft.network.packet.Packet;

public class PacketServerEvent extends Cancellable {
	public Packet<?> packet;
	
	public PacketServerEvent(Packet<?> packet) {
		this.packet = packet;
	}
}