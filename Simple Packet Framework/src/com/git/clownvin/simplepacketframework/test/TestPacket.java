package com.git.clownvin.simplepacketframework.test;

import com.git.clownvin.simplepacketframework.packet.Packet;

public class TestPacket extends Packet {

	public TestPacket(boolean construct, byte[] bytes) {
		super(construct, bytes);
	}

	@Override
	protected void construct(byte[] bytes) {
		//Do nothing
	}

	@Override
	public boolean shouldEncrypt() {
		return true;
	}

}
