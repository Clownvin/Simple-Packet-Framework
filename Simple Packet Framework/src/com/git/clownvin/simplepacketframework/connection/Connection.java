package com.git.clownvin.simplepacketframework.connection;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

import com.git.clownvin.simplepacketframework.packet.Packet;
import com.git.clownvin.simplepacketframework.packet.PacketSystem;
import com.git.clownvin.simplepacketframework.packet.PublicKeyPacket;
import com.git.clownvin.simplepacketframework.packet.Request;
import com.git.clownvin.simplepacketframework.packet.RequestTimedOutException;
import com.git.clownvin.simplepacketframework.packet.Response;
import com.git.clownvin.simplescframework.connection.KeyExchangeIncompleteException;
import com.git.clownvin.simplescframework.connection.PrivateConnection;
import com.clownvin.util.CircularList;

public class Connection extends PrivateConnection {
	
	private Queue<Packet> outgoingPackets;
	private byte[] packetBuffer = new byte[Short.MAX_VALUE];
	private final PacketSystem packetSystem;
	
	public byte[] getPacketBuffer() {
		return packetBuffer;
	}

	public Connection(final Socket socket, final PacketSystem packetSystem) throws IOException {
		super(socket);
		this.packetSystem = packetSystem;
		start();
	}
	
	@Override
	protected void setup() {
		super.setup();
		outgoingPackets = new LinkedList<Packet>();
		send(new PublicKeyPacket(getPublicKey()));
	}

	public final boolean send(final Packet packet) {
		outgoingPackets.add(packet);
		synchronized (outputLock) {
			outputLock.notifyAll();
		}
		return true;
	}
	
	public final Response getResponse(final Request request) throws IOException, InterruptedException, KeyExchangeIncompleteException, RequestTimedOutException {
		return packetSystem.getResponse(this, request);
	}

	@Override
	public boolean readInput() {
		try {
			Packet next = packetSystem.readPacket(Connection.this);
			while (!packetSystem.handlePacket(Connection.this, next));
		} catch (SocketException e) {
			System.out.println(Connection.this + ": " + e.getMessage());
			try {
				kill();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return false;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeyExchangeIncompleteException e) {
			e.printStackTrace();
			send(new PublicKeyPacket(getPublicKey())); //TODO TODO TODO Catching this here means we lose a packet, but it also means we wont be blocked if the server refuses to give us their key
		}
		return true;
	}

	@Override
	public boolean writeOutput() {
		while (outgoingPackets.size() > 0) {
			try {
				packetSystem.writePacket(Connection.this, outgoingPackets.peek());
				//System.out.println("Sent: "+outgoingPackets.peek().getClass());
				outgoingPackets.remove(); //Pop it off since success.
			} catch (SocketException e) {
				System.out.println(Connection.this + ": " + e.getMessage());
				try {
					kill();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return false;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (KeyExchangeIncompleteException e) {
				e.printStackTrace();
				send(new PublicKeyPacket(getPublicKey()));
			}
		}
		try {
			synchronized (outputLock) {
				outputLock.wait(10); //Added a short wait in case a packet gets added but loop wasn't ready.
			}
		} catch (InterruptedException e) {
			try {
				kill();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return false;
		}
		return true;
	}

	@Override
	public void onKill() {
		//Do nothing.
	}
}
