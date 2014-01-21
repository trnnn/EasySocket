
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Date;
import java.util.List;

import easysocket.packet.Packet;
import easysocket.session.AioTcpSession;
import easysocket.session.event.SessionReceivedPacketListener;
import easysocket.utils.StringUtil;

public class ClientTest {

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
