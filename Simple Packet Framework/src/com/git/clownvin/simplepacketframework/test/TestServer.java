package com.git.clownvin.simplepacketframework.test;

import com.git.clownvin.simplepacketframework.connection.Connection;
import com.git.clownvin.simplepacketframework.packet.AbstractPacketHandler;
import com.git.clownvin.simplepacketframework.packet.Packet;
import com.git.clownvin.simplepacketframework.packet.Packets;
import com.git.clownvin.simplepacketframework.packet.PublicKeyPacket;
import com.git.clownvin.simplepacketframework.packet.Request;
import com.git.clownvin.simplescframework.AbstractServer;
import com.git.clownvin.simplescframework.connection.ConnectionAcceptor;

public class TestServer extends AbstractServer {

	public TestServer(String name, int... ports) {
		super(name, ports);
	}

	public TestServer(int... ports) {
		super(ports);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void duringLoop() throws InterruptedException {
		Thread.sleep(1);
		if (System.currentTimeMillis() - time < 10000)
			return;
		System.out.println("Processed "+totalBytes+" bytes in 10 seconds. "+(totalBytes/10)+"b/s");
		totalBytes = 0;
		time = System.currentTimeMillis();
	}

	@Override
	public void atStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void atStop() {
		// TODO Auto-generated method stub

	}
	
	static long totalBytes = 0L;
	static long time = 0L;
	public static void main(String[] args) throws InterruptedException {
		ConnectionAcceptor.setConnectionClass(Connection.class);
		Packets.setPacketDefinition(1, TestPacket.class);
		Packets.setPacketDefinition(0, PublicKeyPacket.class);
		Packets.setPacketDefinition(2, TestRequest.class);
		Packets.setPacketDefinition(3, TestResponse.class);
		//Packets.debug(true);
		Packets.setPacketHandler(new AbstractPacketHandler() {

			@Override
			public boolean handlePacket(Connection source, Packet packet) {
				totalBytes += packet.getBytes().length + 6; //2 bytes start, 2 bytes len, 2 bytes type
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
				totalBytes += packet.getBytes().length + 4;
				source.send(new TestResponse(packet.getReqID()));
				return true;
			}
			
		});
		TestServer server = new TestServer("Test", 6667);
		server.start();
	}

}
