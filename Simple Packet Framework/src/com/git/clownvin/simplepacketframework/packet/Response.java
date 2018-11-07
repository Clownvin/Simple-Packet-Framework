package com.git.clownvin.simplepacketframework.packet;

public abstract class Response extends Packet {

	public Response(boolean construct, byte[] bytes) {
		super(construct, bytes);
	}
	
	public abstract long getReqID();

}
