/*
 * EasySocket FixedSizeSerializable.java
 *
 * Copyright (c) 2014, Qingfeng Lee
 * PROJECT DESCRIPTION
 * 
 * See LICENSE file for more information
 */
package easysocket.serialize;

public interface FixedSizeSerializable extends Serializable {
	public int getSize();
}
