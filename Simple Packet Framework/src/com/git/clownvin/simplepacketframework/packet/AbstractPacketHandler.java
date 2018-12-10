package com.git.clownvin.simplepacketframework.packet;

import com.git.clownvin.simplepacketframework.connection.Connection;

public abstract class AbstractPacketHandler<ConnectionT extends Connection, PacketT extends Packet> {
	public abstract boolean handlePacket(final ConnectionT source, final PacketT packet);
}
