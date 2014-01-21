package easysocket.session.event;

import java.util.List;

import easysocket.packet.Packet;

public abstract class SessionReceivedPacketListener implements
		SessionEventListener {
	@Override
	public void onClose() {
		// do nothing
	}

	@Override
	public abstract void onReceivePackets(List<Packet> packets);

}
