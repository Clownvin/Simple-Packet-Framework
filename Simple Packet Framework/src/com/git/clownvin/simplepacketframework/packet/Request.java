package com.git.clownvin.simplepacketframework.packet;

public abstract class Request extends Packet {
	
	protected long reqID = 0L;

	public Request(boolean construct, byte[] bytes) {
		super(construct, bytes);
		reqID = System.nanoTime();
	}
	
	public long getReqID() {
		return reqID;
	}

}
