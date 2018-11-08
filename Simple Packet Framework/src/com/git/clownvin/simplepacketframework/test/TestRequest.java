package com.git.clownvin.simplepacketframework.test;

import com.git.clownvin.simplepacketframework.packet.Request;

public class TestRequest extends Request {
	
	public TestRequest(byte[] payload) {
		this(false, payload);
	}
	
	public TestRequest(boolean construct, byte[] bytes) {
		super(construct, bytes);
	}

	@Override
	protected void construct(byte[] bytes) {
		//DO nothing.
	}

	@Override
	public boolean shouldEncrypt() {
		return false;
	}

}
