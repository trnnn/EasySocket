package easysocket.packet;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import easysocket.session.AioTcpSession;

public class Packet {
	static final Logger logger = LoggerFactory.getLogger(Packet.class);
	private final short cmd;
	private final ByteBuffer buffer;
	private AioTcpSession session;

	public Packet(short cmd, AioTcpSession session) {
		this(cmd, Short.MAX_VALUE, session);
	}

	public Packet(short cmd, byte[] data, AioTcpSession session) {
		this.cmd = cmd;
		this.session = session;
		buffer = ByteBuffer.wrap(data);
		buffer.order(AioTcpSession.BYTE_ORDER);
	}

	public Packet(short cmd, int initialBufferSize, AioTcpSession session) {
		this.cmd = cmd;
		this.session = session;
		buffer = ByteBuffer.allocate(initialBufferSize);
		buffer.order(AioTcpSession.BYTE_ORDER);
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
