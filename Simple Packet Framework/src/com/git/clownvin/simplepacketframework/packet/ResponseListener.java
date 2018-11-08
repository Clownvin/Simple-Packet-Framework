package com.git.clownvin.simplepacketframework.packet;

import com.git.clownvin.simplepacketframework.connection.Connection;

public final class ResponseListener {
	
	private static long requestTimeout = 5000;
	
	private final long reqID;
	private final Connection source;
	private Response response = null;
	
	public ResponseListener(long reqID, Connection source) {
		this.reqID = reqID;
		this.source = source;
	}
	
	public long getReqID() {
		return reqID;
	}
	
	public boolean matches(Response response, Connection source) {
		return response.getReqID() == reqID && this.source == source;
	}
	
	public void setResponse(Response response) {
		this.response = response;
		synchronized (this) {
			this.notifyAll();
		}
	}
	
	public Response getResponse() throws InterruptedException, RequestTimedOutException {
		long start = System.currentTimeMillis();
		while (response == null && System.currentTimeMillis() - start < requestTimeout) {
			synchronized (this) {
				this.wait(1);
			}
		}
		if (response == null)
			throw new RequestTimedOutException("Request timed out.");
		Response r = response;
		response = null;
		return r;
	}
}
