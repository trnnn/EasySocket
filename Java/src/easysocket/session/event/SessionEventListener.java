package easysocket.session.event;

import java.util.List;

import easysocket.packet.Packet;

public interface SessionEventListener {
	void onClose();
	void onReceivePackets(List<Packet> packets);
}