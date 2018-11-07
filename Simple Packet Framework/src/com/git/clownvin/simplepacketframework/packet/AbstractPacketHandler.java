package com.git.clownvin.simplepacketframework.packet;

import com.git.clownvin.simplepacketframework.connection.Connection;

public abstract class AbstractPacketHandler {
	public abstract boolean handlePacket(final Connection source, final Packet packet);
	
	public abstract boolean handleRequest(final Connection source, final Request packet);
}
