/*
 * EasySocket Packet.java
 *
 * Copyright (c) 2014, Qingfeng Lee
 * PROJECT DESCRIPTION
 * 
 * See LICENSE file for more information
 */
package easysocket.packet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import easysocket.session.AioTcpSession;

public class Packet {
	static final Logger logger = LoggerFactory.getLogger(Packet.class);
	private final short cmd;
	private final ByteBuffer buffer;
	private AioTcpSession session;
	private static final ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

	public Packet(short cmd, AioTcpSession session) {
		this(cmd, Short.MAX_VALUE, session);
	}

	public Packet(short cmd, byte[] data, AioTcpSession session) {
		this.cmd = cmd;
		this.session = session;
		buffer = ByteBuffer.wrap(data);
		buffer.order(byteOrder);
	}

	public Packet(short cmd, int initialBufferSize, AioTcpSession session) {
		this.cmd = cmd;
		this.session = session;
		buffer = ByteBuffer.allocate(initialBufferSize);
		buffer.order(byteOrder);
	}

	public ByteBuffer getByteBuffer() {
		return this.buffer;
	}

	public short getCmd() {
		return cmd;
	}

	public AioTcpSession getSession() {
		return session;
	}

	public void session(AioTcpSession session) {
		this.session = session;
	}
}
