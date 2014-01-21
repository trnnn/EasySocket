/*
 * EasySocket StringUtil.java
 *
 * Copyright (c) 2014, Qingfeng Lee
 * PROJECT DESCRIPTION
 * 
 * See LICENSE file for more information
 */
package easysocket.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtil {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory
			.getLogger(StringUtil.class);
	static final Charset defaultCharset = Charset.forName("UTF-8");

	public static String getString(ByteBuffer bb) {
		final short length = bb.getShort();
		if (length == 0) {
			return "";
		}
		byte[] str = new byte[length];
		bb.get(str, 0, length);
		return new String(str, defaultCharset);
	}

	public static void putString(ByteBuffer bb, String str) {
		if (str == null || str.equals("")) {
			bb.putShort((short) 0);
			return;
		}
		short size = (short) str.getBytes(defaultCharset).length;
		bb.putShort(size);
		if (size > 0)
			bb.put(str.getBytes(defaultCharset));
	}
}
