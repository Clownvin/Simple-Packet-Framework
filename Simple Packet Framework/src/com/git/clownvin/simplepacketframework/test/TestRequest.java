package com.git.clownvin.simplepacketframework.test;

import java.nio.ByteBuffer;

import com.git.clownvin.simplepacketframework.packet.Request;

public class TestRequest extends Request {
	
	private long reqID;
	
	public TestRequest(long reqID) {
		this(false, ByteBuffer.allocate(8).putLong(reqID).array());
		this.reqID = reqID;
	}
	
	public TestRequest(boolean construct, byte[] bytes) {
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
