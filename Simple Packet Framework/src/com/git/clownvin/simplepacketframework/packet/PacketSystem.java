package com.git.clownvin.simplepacketframework.packet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.util.Hashtable;

import com.git.clownvin.simplepacketframework.connection.Connection;
import com.git.clownvin.simplescframework.connection.KeyExchangeIncompleteException;

public abstract class PacketSystem {

	protected Hashtable<Short, Class<? extends Packet>> classDefinitions = new Hashtable<>();
	protected Hashtable<Class<? extends Packet>, Short> typeDefinitions = new Hashtable<>();
	protected Hashtable<Class<? extends Packet>, IPacketHandler<? extends Connection, ? extends Packet>> packetHandlers = new Hashtable<>();
	protected Hashtable<Long, ResponseListener> listeners = new Hashtable<>();
	
	@SuppressWarnings("unchecked")
	protected IPacketHandler<Connection, Packet> getPacketHandler(Class<? extends Packet> cls) {
		return (IPacketHandler<Connection, Packet>) packetHandlers.get(cls);
	}
	
	protected void addResponseListener(ResponseListener listener) {
		listeners.put(listener.getReqID(), listener);
	}
	
	protected void removeResponseListener(ResponseListener listener) {
		listeners.remove(listener.getReqID());
	}
	
	public boolean handlePacket(Connection source, final Packet packet) {
		if (packet instanceof Response) {
			var listener = listeners.get(((Response)packet).getReqID());
			if (listener != null) {
				listener.setResponse((Response) packet);
				return true;
			}
		}
		var packetHandler = getPacketHandler(packet.getClass());
		if (packetHandler == null) {
			throw new RuntimeException("Packets has no packet handler for packet class: "+packet.getClass());
		}
		return packetHandler.handlePacket(source, packet);
	}
	
	public PacketSystem() {
		setPacketDefinition(0, PublicKeyPacket.class);
		setPacketHandler(PublicKeyPacket.class, new IPacketHandler<Connection, PublicKeyPacket>() {

			@Override
			public boolean handlePacket(Connection source, PublicKeyPacket packet) {
				source.finishKeyExchange(packet.getKey());
				return true;
			}
			
		});
		setupPackets();
	}
	
	public abstract void setupPackets();

	public <PacketT extends Packet> void setPacketHandler(final Class<PacketT> cls, final IPacketHandler<? extends Connection, PacketT> packetHandler) {
		packetHandlers.put(cls, packetHandler);
	}
	
	public void setPacketDefinition(int type, final Class<? extends Packet> cls) {
		if (type <= 0 && cls != PublicKeyPacket.class)
			throw new IllegalArgumentException("Type must be a short value greater than 0.");
		var _type = (short) type;
		classDefinitions.put(_type, cls);
		typeDefinitions.put(cls, _type);
	}

	private Class<? extends Packet> getClassForType(final short type) {
		if (!classDefinitions.containsKey(type))
			throw new RuntimeException("No class definition for type: "+type);
		return classDefinitions.get(type);
	}
	
	private short getTypeForClass(Class<? extends Packet> cls) {
		if (!typeDefinitions.containsKey(cls))
			throw new RuntimeException("No type definition for class: "+cls);
		return typeDefinitions.get(cls);
	}

	/*
	 * -- Write-To-Stream Layout -- 
	 * DATA TYPE - SIZE (CUMULATIVE) 
	 * type		 - 2 	(2) 
	 * size 	 - 2 	(4) 
	 * data 	 - ? 	(4 + data.len);
	 */

	public Packet readPacket(final Connection connection) throws IOException, KeyExchangeIncompleteException {
		var in = connection.getInputStream();
		int count = 0;
		while (count < 2) {
			if ((byte) in.read() == (byte) '\n')
				count++;
			else
				count = 0;
		}
		byte[] buffer = connection.getPacketBuffer();
		in.read(buffer, 0, 2);
		boolean decrypt = (buffer[0] & 0x80) > 0;
		short type = (short) (((buffer[0] & 0x7F) << 8) | (buffer[1] & 0xFF));
		in.read(buffer, 0, 2);
		short size = (short) (((buffer[0] & 0xFF) << 8) | (buffer[1] & 0xFF));
		//System.out.println("Read packet with size: "+size);
		//System.out.println("rSize: "+Integer.toHexString(size)+", "+Integer.toHexString(((buffer[0] & 0xFF) << 8))+", "+Integer.toHexString((buffer[1] & 0xFF)));
		if (size == -1)
			throw new SocketException("Socket wants to close.");
		if (size < 0) {
			throw new RuntimeException("Packet size cannot be negative. Size: " + size+", Type: "+type);
		}
		in.read(buffer, 0, size);
		if (decrypt) {
			buffer = connection.decrypt(buffer, size);
			size = (short) buffer.length;
		}
		Class<? extends Packet> cls = getClassForType(type);
		if (cls == null) {
			System.err.println("No packet definition for type: " + type + ". Add one using Packets.setPacketDefinition(int type, Class<? extends Packet>) cls)");
			throw new RuntimeException("No packet definition for type: " + type + ". Add one using Packets.setPacketDefinition(int type, Class<? extends Packet>) cls)");
		}
		try {
			Packet packet = cls.getConstructor(boolean.class, byte[].class, int.class).newInstance(true, buffer, size);
			return packet;
		} catch (NoSuchMethodException e) {
			System.out.println("Packet " + cls + "(type: " + type
					+ ") doesn't have a proper constructor! Please add a constructor with the signature: <init>(boolean, byte[][]).");
			System.out.println("You should also just call the superconstructor with the same signature, it will handle the rest.");
			System.out.println("Example: Constructor(boolean construct, byte[][] bytes) { super(construct, bytes); }");
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| SecurityException e) {
			e.printStackTrace();
			System.exit(1);
		}
		throw new IOException("Failed to read packet.");
	}
	
	public Response getResponse(final Connection connection, final Request request) throws IOException, KeyExchangeIncompleteException, RequestTimedOutException, InterruptedException {
		ResponseListener listener = new ResponseListener(request.getReqID(), connection);
		addResponseListener(listener);
		writePacket(connection, request);
		Response response;
		try {
			response = listener.getResponse();
		} catch (RequestTimedOutException e) {
			removeResponseListener(listener);
			throw e;
		}
		removeResponseListener(listener);
		return response;
	}

	public void writePacket(final Connection connection, final Packet packet) throws IOException, KeyExchangeIncompleteException {
		var buffer = new byte[2];
		var out = connection.getOutputStream();
		short s = getTypeForClass(packet.getClass());
		out.write((byte) '\n'); //Signal of the beginning of new packet
		out.write((byte) '\n');
		buffer[0] = (byte) ((s >>> 8) | (packet.shouldEncrypt() ? 0x80 : 0));
		buffer[1] = (byte) (s & 0xFF);
		out.write(buffer);
		byte[] data = packet.shouldEncrypt() ? connection.encrypt(packet.getBytes()) : packet.getBytes();
		s = (short) data.length;
		//System.out.println("Wrote packet with size: "+s);
		buffer[0] = (byte) (s >>> 8);
		buffer[1] = (byte) (s & 0xFF);
		//System.out.println("wSize: "+Integer.toHexString(s)+", "+Integer.toHexString(buffer[0])+", "+Integer.toHexString(buffer[0])+" ::: "+Integer.toHexString(((buffer[0] & 0xFF) << 8) | (buffer[1] & 0xFF)));
		out.write(buffer);
		out.write(data);
		out.flush();
	}
}
