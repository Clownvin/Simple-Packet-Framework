package com.git.clownvin.simplepacketframework.packet;

import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class Request extends Packet {
	
	protected long reqID = 0L;

	public Request(boolean construct, byte[] bytes) {
		super(construct, construct ? Arrays.copyOfRange(bytes, 8, bytes.length) : bytes);
		if (construct) {
			reqID = ByteBuffer.wrap(bytes).getLong();
			this.bytes = bytes;
			//System.out.println("Constructing request with blen: "+bytes.length);
		} else {
			reqID = System.nanoTime();
			this.bytes = combine(ByteBuffer.allocate(8).putLong(reqID).array(), bytes);
			//System.out.println("Creating request with blen: "+bytes.length);
		}
		//
	}
	
	public long getReqID() {
		return reqID;
	}

}
