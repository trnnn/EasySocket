package easysocket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import easysocket.session.AioTcpSession;
import easysocket.utils.PrintStackTrace;

public class SocketServer {

	public static interface ClientConnectedEventHandler {
		public void OnConnect(AioTcpSession session);
	}

	private CompletionHandler<AsynchronousSocketChannel, Void> acceptCompletionHanlder = new CompletionHandler<AsynchronousSocketChannel, Void>() {

		@Override
		public void completed(AsynchronousSocketChannel channel, Void attachment) {
			SocketServer.this.pendingAccept();

			AioTcpSession session = new AioTcpSession(channel);
			session.pendingRead();
			int sessionId = session.getSessionId();
			SESSION_POOL.put(sessionId, session);

			if (connectedEventHandler != null) {
				connectedEventHandler.OnConnect(session);
			}
		}

		@Override
		public void failed(Throwable exc, Void attachment) {
			PrintStackTrace.print(logger, exc);
		}
	};

	static Logger logger = LoggerFactory.getLogger(SocketServer.class);
	private final Map<Integer, AioTcpSession> SESSION_POOL = new ConcurrentHashMap<>();

	private AsynchronousServerSocketChannel server;
	private final ClientConnectedEventHandler connectedEventHandler;
	private final int port;

	public SocketServer(int port,
			ClientConnectedEventHandler connectedEventHandler) {
		this.port = port;
		this.connectedEventHandler = connectedEventHandler;
	}

	public void start() throws IOException {
		int initialSize = Runtime.getRuntime().availableProcessors();
		AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup
				.withCachedThreadPool(Executors.newCachedThreadPool(),
						initialSize);
		AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel
				.open(channelGroup);
		server.bind(new InetSocketAddress(port));

		this.pendingAccept();
		logger.debug("server started at port " + this.port);
	}

	private void pendingAccept() {
		if (this.server == null) {
			throw new NullPointerException(
					"server is null, invoke start before pendingAccept");
		}

		this.server.accept(null, this.acceptCompletionHanlder);
	}

	public AioTcpSession findClient(int sessionId) {
		return this.SESSION_POOL.get(sessionId);
	}

	public void stop() throws IOException {
		if (this.server == null) {
			throw new IllegalStateException("server never opend");
		}
		this.server.close();
	}
}
