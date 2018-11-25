package com.git.clownvin.simplepacketframework.packet;

import java.nio.ByteBuffer;

public abstract class Response extends Packet {
	
	protected long reqID;
	
	public Response(long reqID) {
		super(false, ByteBuffer.allocate(8).putLong(reqID).array(), 8);
		this.reqID = reqID;
	}
	
	public Response(boolean construct, byte[] bytes, int length) {
		super(construct, bytes, length);
	}
	
	public void preConstructor(byte[] bytes) {
		reqID = ByteBuffer.wrap(bytes).getLong();
	}
	
	public long getReqID() {
		return reqID;
	}

}
