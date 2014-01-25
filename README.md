EasySocket
==========

Build socket based applications easily.


Hello Fxxk World!!




``` java

public class ServerTest {

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

Checkout and compile the project:

``` bash
git clone git@github.com:trnnn/EasySocket.git
cd EasySocket
ant jar
mv jar/easysocket.jar /path/to/your/libs/project
```
