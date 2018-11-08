package com.git.clownvin.simplepacketframework.packet;

import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class Response extends Packet {
	
	protected long reqID;
	
	public Response(long reqID) {
		super(false, new byte[0]);
		this.reqID = reqID;
		this.bytes = combine(ByteBuffer.allocate(8).putLong(reqID).array(), bytes);
	}
	
	public Response(boolean construct, byte[] bytes) {
		super(construct, construct ? Arrays.copyOfRange(bytes, 8, bytes.length) : bytes);
		if (construct) {
			reqID = ByteBuffer.wrap(bytes).getLong();
		} else {
			throw new IllegalArgumentException("Response(bool, byte[]) must always be used as construction constructor!");
		}
	}
	
	public long getReqID() {
		return reqID;
	}

}
