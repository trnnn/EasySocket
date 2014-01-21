/*
 * EasySocket AioTcpSession.java
 *
 * Copyright (c) 2014, Qingfeng Lee
 * PROJECT DESCRIPTION
 * 
 * See LICENSE file for more information
 */
package easysocket.session;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadPendingException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import easysocket.packet.Packet;
import easysocket.packet.PacketBuilder;
import easysocket.session.event.SessionEventListener;
import easysocket.session.event.SessionReceivedPacketListener;
import easysocket.utils.PrintStackTrace;

/**
 * Use <code>AioTcpSession</code> to store the information of a socket
 * connection
 * 
 * Invoke {@link #onReceivePacket(SessionReceivedPacketListener)} to
 * specify packet received event lister
 * 
 * @see SessionReceivedPacketListener
 */
public class AioTcpSession {

	static final Logger logger = LoggerFactory.getLogger(AioTcpSession.class);

	private class WriteCompletionHandler implements
			CompletionHandler<Integer, AioTcpSession> {
		@Override
		public void completed(Integer result, AioTcpSession session) {
			if (result < 0 || !session.getChannel().isOpen()) {
				try {
					close();
				} catch (IOException e) {
					logger.error("WriteCompletionHandler.completed, msg:"
							+ e.getMessage());
					PrintStackTrace.print(logger, e);
				}
				return;
			}
			if (writeBuffer != null) {
				writeBuffer.position(result);
			}
			isWriting(false);
			write();
		}

		@Override
		public void failed(Throwable exc, AioTcpSession session) {
			try {
				close();
			} catch (IOException e) {
				logger.error("WriteCompletionHandler.failed, msg:"
						+ e.getMessage());
				PrintStackTrace.print(logger, e);
			}
		}
	}

	private class ReadComletionHandler implements
			CompletionHandler<Integer, AioTcpSession> {
		@Override
		public void completed(Integer result, AioTcpSession session) {
			try {
				if (result > 0) {
					session.pendingRead();
				} else {
					session.close();
				}
			} catch (IOException e) {
				logger.error("ReadComletionHandler.completed. msg:"
						+ e.getMessage());
				PrintStackTrace.print(logger, e);
				try {
					session.close();
				} catch (IOException e1) {
					logger.error("ReadComletionHandler.completed. msg:"
							+ e1.getMessage());
					PrintStackTrace.print(logger, e1);
				}
			} catch (Exception e) {
				logger.error("ReadComletionHandler.completed. msg:"
						+ e.getMessage());
				PrintStackTrace.print(logger, e);
				try {
					session.close();
				} catch (IOException e1) {
					logger.error("ReadComletionHandler.completed. msg:"
							+ e.getMessage());
					PrintStackTrace.print(logger, e);
				}
			}
		}

		@Override
		public void failed(Throwable exc, AioTcpSession session) {
			try {
				session.close();
			} catch (IOException e) {
				logger.error("ReadComletionHandler.failed. msg:"
						+ e.getMessage());
				PrintStackTrace.print(logger, e);
			} catch (Exception e) {
				logger.error("ReadComletionHandler.failed. msg:"
						+ e.getMessage());
				PrintStackTrace.print(logger, e);
				try {
					session.close();
				} catch (IOException e1) {
					logger.error("ReadComletionHandler.failed. msg:"
							+ e.getMessage());
					PrintStackTrace.print(logger, e);
				}
			}
		}
	}

	private ByteBuffer readBuffer = ByteBuffer.allocate(256);
	protected AsynchronousSocketChannel channel;
	protected AtomicBoolean isWriting = new AtomicBoolean(false);
	protected final int sessionId;
	protected CompletionHandler<Integer, AioTcpSession> readCompletionHandler;
	protected CompletionHandler<Integer, AioTcpSession> writeCompletionHandler;
	private final ConcurrentLinkedQueue<ByteBuffer> outs = new ConcurrentLinkedQueue<>();
	private ByteBuffer writeBuffer;
	protected static final AtomicInteger sessionIndex = new AtomicInteger(1);
	public final SessionState sessionState = new SessionState(
			SessionState.UNKNOWN);
	private List<SessionEventListener> eventListeners = new CopyOnWriteArrayList<>();
	private boolean hasStartedReading = false;

	private static final PacketBuilder PACKET_BUILDER = new PacketBuilder();

	public void registerEventListener(SessionEventListener listener) {
		this.eventListeners.add(listener);
	}

	public void unregisterEventListener(SessionEventListener listener) {
		this.eventListeners.remove(listener);
	}

	public void onReceivePacket(SessionReceivedPacketListener listener) {
		this.eventListeners.add(listener);
	}

	public AioTcpSession(AsynchronousSocketChannel channel) {
		this.sessionId = sessionIndex.addAndGet(1);
		this.readCompletionHandler = new ReadComletionHandler();
		this.writeCompletionHandler = new WriteCompletionHandler();
		this.channel = channel;
		this.sessionState.set(SessionState.OPENED);
	}

	protected void queuePacket(ByteBuffer buffer) {
		List<Packet> packets = PACKET_BUILDER.parse(buffer, this);
		if (packets == null || packets.size() <= 0) {
			return;
		}

		this.onReceivedPackets(packets);
	}

	private void onReceivedPackets(List<Packet> packets) {
		for (SessionEventListener listener : this.eventListeners) {
			listener.onReceivePackets(packets);
		}
	}

	private void beforeRead(ByteBuffer readBuffer) {
		readBuffer.flip();
		if (readBuffer.hasRemaining()) {
			this.queuePacket(readBuffer);
			// Check readBuffer is full
			if (readBuffer.position() > 4
					&& readBuffer.getInt(0) > readBuffer.capacity()) {
				extendReadBuffer();
			}
		} else {
			readBuffer.clear();
		}
	}

	public final void pendingRead() {
		if (hasStartedReading) {
			logger.error("this session has started reading already");
			throw new ReadPendingException();
		}
		hasStartedReading = true;
		beforeRead(this.readBuffer);
		// if (this.channel.isOpen())
		this.channel.read(this.readBuffer, this, this.readCompletionHandler);
	}

	private final void extendReadBuffer() {
		ByteBuffer tmp = readBuffer;
		int pos = tmp.position();
		tmp.position(0);

		readBuffer = ByteBuffer.allocate(readBuffer.capacity() * 2);
		readBuffer.put(tmp.array(), 0, pos);
	}

	public void close() throws IOException {
		this.onClose();
		if (this.channel.isOpen())
			this.channel.close();
	}

	private void onClose() {
		for (SessionEventListener listener : this.eventListeners) {
			listener.onClose();
		}
	}

	protected final void pushWriteData(ByteBuffer buffer) {
		outs.add(buffer);
		write();
	}

	public void sendPacket(Packet packet) {
		ByteBuffer buffer = this.formatSendPacket(packet.getCmd(), packet
				.getByteBuffer().array());
		this.pushWriteData(buffer);
	}

	private ByteBuffer formatSendPacket(short cmd, byte[] data) {
		int packetSize = 4 + 2 + data.length;
		ByteBuffer buffer = ByteBuffer.allocate(packetSize);
		buffer.putInt(packetSize);
		buffer.putInt(cmd);
		buffer.put(data);
		return buffer;
	}

	private final void write() {
		if (isWriting.compareAndSet(false, true)) {
			if (this.writeBuffer != null) {
				int pos = this.writeBuffer.position();
				int limit = this.writeBuffer.limit();
				if (pos >= limit) {
					this.writeBuffer = null;
				} else {
					this.writeBuffer.compact();
					this.writeBuffer.flip();
					this.channel.write(this.writeBuffer, this,
							this.writeCompletionHandler);
					return;
				}
			}
			ByteBuffer buffer = outs.poll();
			if (buffer != null) {
				this.writeBuffer = buffer;
				int cmd = this.writeBuffer
						.getShort(this.writeBuffer.position() + 4);
				if (cmd == 0) {
					logger.error(
							"AioSession.write, send packet cmd 0, data->{}",
							new String(this.writeBuffer.array()));
				}
				this.channel.write(buffer, this, this.writeCompletionHandler);
			} else {
				isWriting.set(false);
			}
		}
	}

	public final boolean isWriting() {
		return this.isWriting.get();
	}

	public final void isWriting(boolean isWriting) {
		this.isWriting.set(isWriting);
	}

	public final boolean isChannelOpen() {
		return channel.isOpen();
	}

	public final AsynchronousSocketChannel getChannel() {
		return channel;
	}

	public final void setChannel(AsynchronousSocketChannel channel) {
		this.channel = channel;
	}

	public final int getSessionId() {
		return sessionId;
	}

	public final String remoteAddress() {
		try {
			InetSocketAddress sa = (InetSocketAddress) this.channel
					.getRemoteAddress();
			String addr = sa.getAddress().getHostAddress();
			return addr;
		} catch (IOException e) {
			logger.error("AioSession.remoteAddress, " + e.getMessage());
			PrintStackTrace.print(logger, e);
		}
		return "";
	}

}
