package com.git.clownvin.simplepacketframework.test;

import java.nio.ByteBuffer;

import com.git.clownvin.simplepacketframework.packet.Response;

public class TestResponse extends Response {
	
	private long reqID;
	
	public TestResponse(long reqID) {
		this(false, ByteBuffer.allocate(8).putLong(reqID).array());
		this.reqID = reqID;
	}

	public TestResponse(boolean construct, byte[] bytes) {
		super(construct, bytes);
	}

	@Override
	public long getReqID() {
		return reqID;
	}

	@Override
	protected void construct(byte[] bytes) {
		reqID = ByteBuffer.wrap(bytes).getLong();
	}

	@Override
	public boolean shouldEncrypt() {
		return false;
	}

}
