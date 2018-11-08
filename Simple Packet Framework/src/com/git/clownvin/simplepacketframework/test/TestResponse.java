package com.git.clownvin.simplepacketframework.test;

import com.git.clownvin.simplepacketframework.packet.Response;

public class TestResponse extends Response {
	
	public TestResponse(long reqID) {
		super(reqID);
	}
	
	public TestResponse(boolean construct, byte[] bytes) {
		super(construct, bytes);
	}

	@Override
	protected void construct(byte[] bytes) {
		//DO nothing
	}

	@Override
	public boolean shouldEncrypt() {
		return false;
	}

}
