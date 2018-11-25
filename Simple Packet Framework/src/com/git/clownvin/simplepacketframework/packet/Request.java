package com.git.clownvin.simplepacketframework.packet;

import java.nio.ByteBuffer;

public abstract class Request extends Packet {
	
	protected long reqID;

	public Request(boolean construct, byte[] bytes, int length) {
		super(construct, bytes, length);
		if (!construct) {
			reqID = System.nanoTime();
			this.bytes = combine(ByteBuffer.allocate(8).putLong(reqID).array(), bytes);
		}
	}
	
	protected void preConstructor(byte[] bytes) {
		reqID = ByteBuffer.wrap(bytes).getLong();
	}
	
	public long getReqID() {
		return reqID;
	}

}
