/*
 * EasySocket PacketBuilder.java
 *
 * Copyright (c) 2014, Qingfeng Lee
 * PROJECT DESCRIPTION
 * 
 * See LICENSE file for more information
 */
package easysocket.packet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import easysocket.session.AioTcpSession;
import easysocket.utils.PrintStackTrace;

public class PacketBuilder {

	static final Logger logger = LoggerFactory.getLogger(PacketBuilder.class);

	public final List<Packet> parse(ByteBuffer buffer, AioTcpSession session) {
		List<Packet> packets = new ArrayList<>();

		boolean loop = true;
		int remaining = 0;

		while (loop) {
			remaining = buffer.remaining();

			if (remaining >= 8) {
				int len = buffer.getInt(buffer.position());
				if (len < 8) {
					logger.error(
							"PakcetBuilder.parse, illegal,  length:{}, data ->{}",
							len, buffer.array());
					try {
						session.close();
					} catch (IOException e) {
						logger.error("PacketBuilder.parse, " + e.getMessage());
						PrintStackTrace.print(logger, e);
					}
					return Collections.emptyList();
				}
				if (remaining >= len) {
					buffer.getInt(); // to forward position
					int cmd = buffer.getInt();
					byte[] data = new byte[len - 8];
					buffer.get(data);

					packets.add(new Packet(cmd, data, session));
				} else {
					loop = false;
				}
			} else {
				loop = false;
			}
		}

		buffer.compact();
		buffer.clear();
		buffer.position(remaining);

		return packets;
	}
}
