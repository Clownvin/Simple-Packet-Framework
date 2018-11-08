package com.git.clownvin.simplepacketframework.packet;

import java.util.Base64;

public abstract class Packet {
	
	protected static final byte[] combine(byte[] ... bytes) {
		if (bytes.length == 1)
			return bytes[0];
		int length = 0;
		for (byte[] b : bytes)
			length += b.length;
		byte[] nBytes = new byte[length];
		int i = 0;
		for (byte[] b : bytes)
			for (byte c : b)
				nBytes[i++] = c;
		return nBytes;
	}
	
	protected byte[] bytes;

	public Packet(boolean construct, final byte[] bytes) {
		this.bytes = bytes;
		//System.out.println("Initializing packet with len: "+bytes.length);
		if (construct)
			construct(this.bytes);
	}

	protected abstract void construct(final byte[] bytes);

	public final int getType() {
		return Packets.getTypeForClass(this.getClass());
	}

	public final byte[] getBytes() {
		return bytes;
	}

	public final short sizeOf() {
		return (short) bytes.length;
	}
	
	public abstract boolean shouldEncrypt();
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Type: " + getType() + ", Size: " + sizeOf() + ", Data:\n");
		builder.append(Base64.getEncoder().encodeToString(bytes));
		builder.append('\n');
		char[] chars = new char[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			chars[i] = (char) bytes[i];
		}
		builder.append(String.valueOf(chars));
		return builder.toString();
	}
}
