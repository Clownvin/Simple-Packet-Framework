package com.git.clownvin.simplepacketframework.packet;

import com.git.clownvin.simplepacketframework.connection.Connection;

public interface IPacketHandler<ConnectionT extends Connection, PacketT extends Packet> {
	public boolean handlePacket(final ConnectionT source, final PacketT packet);
}
