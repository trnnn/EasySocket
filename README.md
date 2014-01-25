EasySocket
==========

EasySocket is an easy way to build socket based applications, it provides reliable data communication.

Features:

 * __stream packet__ - EasySocket provides a binary stream packet formation,


## How to use

Using Easysocket is quite simple. But lets see:

* __Server:__

``` java
public class ServerDemo {

	public static void main(String[] args) throws IOException,
			InterruptedException {
		SocketServer server = new SocketServer(8000,
				new ClientConnectedEventHandler() {
					@Override
					public void OnConnect(final AioTcpSession session) {
						session.registerEventListener(new SessionReceivedPacketListener() {

							@Override
							public void onReceivePackets(List<Packet> packets) {
								for (Packet packet : packets) {
									String msg = StringUtil.getString(packet
											.getByteBuffer());
									System.out.println("client:" + msg);
								}
							}
						});
						Thread thread = new Thread(new Runnable() {
							@Override
							public void run() {
								while (true) {

									String msg = "this is message from server, "
											+ new Date();
									Packet packet = new Packet(1000, 4 + msg
											.length(), session);
									StringUtil.putString(
											packet.getByteBuffer(), msg);
									session.sendPacket(packet);
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}
						});
						thread.start();
					}
				});
		server.start();
		Thread.currentThread().join();
	}
}	
```

* __Client:__

``` java
public class ClientDemo {

	public static void main(String[] args) throws IOException, InterruptedException {
		AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
		client.connect(new InetSocketAddress("127.0.0.1", 8000));
		final AioTcpSession session = new AioTcpSession(client);
		session.pendingRead();
		session.registerEventListener(new SessionReceivedPacketListener() {

			@Override
			public void onReceivePackets(List<Packet> packets) {
				for (Packet packet : packets) {
					String msg =StringUtil.getString(packet.getByteBuffer());
					System.out.println("server:" + msg);
				}
			}
		});
		Thread thread= new Thread(new Runnable() {
			@Override
			public void run() {
				while(true){
					String msg = "this is message from client, "
							+ new Date();
					Packet packet = new Packet(1000,
							4 + msg.length(), session);
					StringUtil.putString(packet.getByteBuffer(), msg);
					session.sendPacket(packet);
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		thread.start();
		Thread.currentThread().join();
	}
}
...


Checkout and compile the project:

 * with git
 
		git clone git://github.com/Gottox/socket.io-java-client.git
