package com.git.clownvin.simplepacketframework.packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;

public final class PublicKeyPacket extends Packet {
	
	private static byte[] toBytes(final PublicKey key) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try(ObjectOutputStream out = new ObjectOutputStream(bos)) {
			out.writeObject(key);
			out.flush();
			byte[] bytes = bos.toByteArray();
			return bytes;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static PublicKey fromBytes(final byte[] bytes) {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		try(ObjectInputStream in = new ObjectInputStream(bis)) {
			return (PublicKey) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private PublicKey key;

	public PublicKeyPacket(final PublicKey key) {
		super(false, toBytes(key));
		this.key = key;
	}
	
	public PublicKeyPacket(boolean construct, byte[] bytes) {
		super(construct, bytes);
	}

	@Override
	protected void construct(byte[] bytes) {
		key = fromBytes(bytes);
	}

	@Override
	public boolean shouldEncrypt() {
		return false;
	}
	
	public PublicKey getKey() {
		return key;
	}

}
