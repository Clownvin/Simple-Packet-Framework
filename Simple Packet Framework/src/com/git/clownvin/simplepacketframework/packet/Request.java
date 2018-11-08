package com.git.clownvin.simplepacketframework.packet;

import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class Request extends Packet {
	
	protected long reqID = 0L;

	public Request(boolean construct, byte[] bytes) {
		super(construct, construct ? Arrays.copyOfRange(bytes, 8, bytes.length) : bytes);
		if (construct) {
			reqID = ByteBuffer.wrap(bytes).getLong();
		} else {
			reqID = System.nanoTime();
			this.bytes = combine(ByteBuffer.allocate(8).putLong(reqID).array(), bytes);
		}
	}
	
	public long getReqID() {
		return reqID;
	}

}
