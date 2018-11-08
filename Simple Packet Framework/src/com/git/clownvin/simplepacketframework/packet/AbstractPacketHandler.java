package com.git.clownvin.simplepacketframework.packet;

import com.git.clownvin.simplepacketframework.connection.Connection;

public abstract class AbstractPacketHandler<ConnectionT extends Connection> {
	public abstract boolean handlePacket(final ConnectionT source, final Packet packet);
	
	public abstract boolean handleRequest(final ConnectionT source, final Request packet);
}
