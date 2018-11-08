package com.git.clownvin.simplepacketframework.packet;

public class RequestTimedOutException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4291320767050567926L;
	
	public RequestTimedOutException(String message) {
		super(message);
	}
	
	public RequestTimedOutException(Exception e) {
		super(e);
	}

}
