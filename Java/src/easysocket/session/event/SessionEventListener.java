/*
 * EasySocket SessionEventListener.java
 *
 * Copyright (c) 2014, Qingfeng Lee
 * PROJECT DESCRIPTION
 * 
 * See LICENSE file for more information
 */
package easysocket.session.event;

import java.util.List;

import easysocket.packet.Packet;

public interface SessionEventListener {
	void onClose();
	void onReceivePackets(List<Packet> packets);
}