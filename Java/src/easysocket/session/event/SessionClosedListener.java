/*
 * EasySocket SessionClosedListener.java
 *
 * Copyright (c) 2014, Qingfeng Lee
 * PROJECT DESCRIPTION
 * 
 * See LICENSE file for more information
 */
package easysocket.session.event;

import java.util.List;

import easysocket.packet.Packet;

public abstract class SessionClosedListener implements SessionEventListener {
	@Override
	public abstract void onClose();

	@Override
	public void onReceivePackets(List<Packet> packets) {
		// do nothing
	}
}
