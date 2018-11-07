package com.git.clownvin.simplepacketframework.packet;

public abstract class Request extends Packet {

	public Request(boolean construct, byte[] bytes) {
		super(construct, bytes);
	}
	
	public abstract long getReqID();

}
