package com.git.clownvin.simplepacketframework.packet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.LinkedList;

import com.git.clownvin.simplepacketframework.connection.Connection;
import com.git.clownvin.simplescframework.connection.KeyExchangeIncompleteException;

@SuppressWarnings("rawtypes")
public final class Packets {

	private static Hashtable<Short, Class<? extends Packet>> classDefinitions = new Hashtable<>();
	private static Hashtable<Class<? extends Packet>, Short> typeDefinitions = new Hashtable<>();
	private static LinkedList<ResponseListener> listeners = new LinkedList<>();
	private static AbstractPacketHandler packetHandler = new AbstractPacketHandler() {

		@Override
		public boolean handlePacket(Connection source, Packet packet) {
			throw new RuntimeException("Packet handler not set! Set one using Packets.setPacketHandler(PacketHandler instance)");
		}

		@Override
		public boolean handleRequest(Connection source, Request packet) {
			throw new RuntimeException("Packet handler not set! Set one using Packets.setPacketHandler(PacketHandler instance)");
		}
		
	};
	
	public static void setPacketHandler(final AbstractPacketHandler<? extends Connection> packetHandler) {
		Packets.packetHandler = packetHandler;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends AbstractPacketHandler<? extends Connection>> T getPacketHandler() {
		return (T) packetHandler;
	}
	
	public static void addResponseListener(ResponseListener listener) {
		listeners.add(listener);
	}
	
	public static void removeResponseListener(ResponseListener listener) {
		listeners.remove(listener);
	}
	
	@SuppressWarnings("unchecked")
	public static boolean handle(Connection source, final Packet packet) {
		if (packet instanceof Request) {
			return packetHandler.handleRequest(source, (Request) packet);
		}
		if (packet instanceof Response) {
			for (ResponseListener listener : listeners) {
				if (listener.matches((Response) packet, source)) {
					listener.setResponse((Response) packet);
					return true;
				}
			}
			return true;
		}
		return packetHandler.handlePacket(source, packet);
	}
	
	static {
		classDefinitions.put((short) 0, PublicKeyPacket.class);
		typeDefinitions.put(PublicKeyPacket.class, (short) 0);
	}

	public static void setPacketDefinition(final int type, final Class<? extends Packet> cls) {
		if (type <= 0)
			throw new IllegalArgumentException("Type must be a short value greater than 0.");
		var _type = (short) type;
		if (!classDefinitions.containsKey(_type))
			classDefinitions.put(_type, cls);
		if (!typeDefinitions.containsKey(cls))
			typeDefinitions.put(cls, _type);
	}

	public static Class<? extends Packet> getClassForType(final short type) {
		if (!classDefinitions.containsKey(type))
			throw new RuntimeException("No class definition for type: "+type);
		return classDefinitions.get(type);
	}
	
	public static short getTypeForClass(Class<? extends Packet> cls) {
		if (!typeDefinitions.containsKey(cls))
			throw new RuntimeException("No type definition for class: "+cls);
		return typeDefinitions.get(cls);
	}

	private static void fillBuffer(byte[] buffer, final InputStream in) throws IOException {
		for (var i = 0; i < buffer.length; i++) {
			buffer[i] = (byte) in.read();
		}
	}

	/*
	 * -- Write-To-Stream Layout -- 
	 * DATA TYPE - SIZE (CUMULATIVE) 
	 * type		 - 2 	(2) 
	 * size 	 - 2 	(4) 
	 * data 	 - ? 	(4 + data.len);
	 */

	public static Packet readPacket(final Connection connection) throws IOException, InterruptedException, KeyExchangeIncompleteException {
		var in = connection.getInputStream();
		int count = 0;
		while (count < 2) {
			if ((byte) in.read() == (byte) '\n')
				count++;
			else
				count = 0;
		}
		var buffer = new byte[2];
		fillBuffer(buffer, in);
		boolean decrypt = (buffer[0] & 0x80) > 0;
		short type = (short) (((buffer[0] & 0x7F) << 8) | (buffer[1] & 0xFF));
		fillBuffer(buffer, in);
		short size = (short) (((buffer[0] & 0xFF) << 8) | (buffer[1] & 0xFF));
		//System.out.println("Read packet with size: "+size);
		//System.out.println("rSize: "+Integer.toHexString(size)+", "+Integer.toHexString(((buffer[0] & 0xFF) << 8))+", "+Integer.toHexString((buffer[1] & 0xFF)));
		if (size == -1)
			throw new SocketException("Socket wants to close.");
		if (size < 0) {
			throw new RuntimeException("Packet size cannot be negative. Size: " + size+", Type: "+type);
		}
		buffer = new byte[size];
		fillBuffer(buffer, in);
		if (decrypt)
			buffer = connection.decrypt(buffer);
		Class<? extends Packet> cls = classDefinitions.get(type);
		if (cls == null) {
			System.err.println("No packet definition for type: " + type + ". Add one using Packets.setPacketDefinition(int type, Class<? extends Packet>) cls)");
			throw new RuntimeException("No packet definition for type: " + type + ". Add one using Packets.setPacketDefinition(int type, Class<? extends Packet>) cls)");
		}
		try {
			Packet packet = cls.getConstructor(boolean.class, byte[].class).newInstance(true, buffer);
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
	
	public static Response getResponse(final Connection connection, final Request request) throws IOException, InterruptedException, KeyExchangeIncompleteException, RequestTimedOutException {
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

	public static void writePacket(final Connection connection, final Packet packet) throws IOException, InterruptedException, KeyExchangeIncompleteException {
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
