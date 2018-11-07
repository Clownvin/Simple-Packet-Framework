package com.git.clownvin.simplepacketframework.packet;

import com.git.clownvin.simplepacketframework.connection.Connection;

public final class ResponseListener {
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
	
	public Response getResponse() throws InterruptedException {
		while (response == null) {
			synchronized (this) {
				this.wait(1);
			}
		}
		Response r = response;
		response = null;
		return r;
	}
}
