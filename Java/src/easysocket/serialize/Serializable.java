/*
 * EasySocket Serializable.java
 *
 * Copyright (c) 2014, Qingfeng Lee
 * PROJECT DESCRIPTION
 * 
 * See LICENSE file for more information
 */
package easysocket.serialize;

import java.nio.ByteBuffer;

public interface Serializable {
	void serialize(ByteBuffer bb);
}
