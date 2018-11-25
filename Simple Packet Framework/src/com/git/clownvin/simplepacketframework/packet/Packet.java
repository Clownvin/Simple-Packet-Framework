package com.git.clownvin.simplepacketframework.packet;

import java.util.Arrays;
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

	public Packet(boolean construct, final byte[] bytes, final int length) {
		this.bytes = bytes;
		//System.out.println("Initializing packet with len: "+bytes.length);
		if (construct) {
			if (this instanceof Request) {
				((Request) this).preConstructor(this.bytes);
				construct(Arrays.copyOfRange(bytes, 8, length), length - 8);
			} else if (this instanceof Response) {
				((Response) this).preConstructor(this.bytes);
				construct(Arrays.copyOfRange(bytes, 8, length), length - 8);
			} else {
				construct(this.bytes, length);
			}
		}
	}

	protected abstract void construct(final byte[] bytes, final int length);

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
