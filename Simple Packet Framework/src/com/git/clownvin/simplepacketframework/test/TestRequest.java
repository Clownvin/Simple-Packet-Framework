package com.git.clownvin.simplepacketframework.test;

import com.git.clownvin.simplepacketframework.packet.Request;

public class TestRequest extends Request {
	
	private byte[] payload;
	
	public TestRequest(byte[] payload) {
		this(false, payload);
		this.payload = payload;
	}
	
	public byte[] getPayload() {
		return payload;
	}
	
	public TestRequest(boolean construct, byte[] bytes) {
		super(construct, bytes);
	}

	@Override
	protected void construct(byte[] bytes) {
		payload = bytes;
	}

	@Override
	public boolean shouldEncrypt() {
		return false;
	}

}
