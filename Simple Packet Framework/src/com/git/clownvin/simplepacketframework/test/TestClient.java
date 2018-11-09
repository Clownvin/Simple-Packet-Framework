package com.git.clownvin.simplepacketframework.test;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Base64;

import com.git.clownvin.simplepacketframework.connection.Connection;
import com.git.clownvin.simplepacketframework.packet.AbstractPacketHandler;
import com.git.clownvin.simplepacketframework.packet.Packet;
import com.git.clownvin.simplepacketframework.packet.Packets;
import com.git.clownvin.simplepacketframework.packet.Request;
import com.git.clownvin.simplepacketframework.packet.RequestTimedOutException;
import com.git.clownvin.simplescframework.connection.KeyExchangeIncompleteException;

public class TestClient {
	
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException, KeyExchangeIncompleteException {
		Packets.setPacketDefinition(1, TestPacket.class);
		Packets.setPacketDefinition(2, TestRequest.class);
		Packets.setPacketDefinition(3, TestResponse.class);
		//Packets.debug(true);
		Packets.setPacketHandler(new AbstractPacketHandler<Connection>() {

			@Override
			public boolean handlePacket(Connection source, Packet packet) {
				switch (packet.getType()) {
				case 0: //Public Key Packet
					break;
				default:
					source.send(packet);
				}
				return true;
			}

			@Override
			public boolean handleRequest(Connection source, Request packet) {
				return true;
			}
			
		});
		
		Connection[] connections = new Connection[28];
		for (int i = 0; i < connections.length; i++) {
			connections[i] = new Connection(new Socket("localhost", 6667));
			//connections[i].send(new TestPacket(false, new byte[25000]));
		}
		while (true) {
			for (int i = 0; i < connections.length; i++) {
				TestRequest req = new TestRequest(new byte[] {1, 2, 3, 4, 5, 6});
				try {
					System.out.println("Sending request with payload: "+Base64.getEncoder().encodeToString(req.getPayload()));
					TestResponse r = (TestResponse) connections[i].getResponse(req);
					System.out.println("Recieved response with payload: "+Base64.getEncoder().encodeToString(r.getPayload()));
					//System.out.println("Sent req with id: "+req.getReqID()+", recieved res with id: "+connections[i].getResponse(req).getReqID());
				} catch (RequestTimedOutException e) {
					System.out.println("Request time out!");
				}
				;
				//connections[i].reconnect();
			}
		}
	}

}
