package com.git.clownvin.simplepacketframework.test;

import java.util.Base64;

import com.git.clownvin.simplepacketframework.packet.Response;

public class TestResponse extends Response {
	
	byte[] payload;
	
	public TestResponse(long reqID, byte[] payload) {
		super(reqID);
		this.payload = payload;
		this.bytes = combine(bytes, payload);
	}
	
	public TestResponse(boolean construct, byte[] bytes) {
		super(construct, bytes);
	}
	
	public byte[] getPayload() {
		return payload;
	}

	@Override
	protected void construct(byte[] bytes) {
		System.out.println("Setting payload: "+Base64.getEncoder().encodeToString(bytes));
		payload = bytes;
	}

	@Override
	public boolean shouldEncrypt() {
		return false;
	}

}
