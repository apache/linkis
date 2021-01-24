/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engine.flink.client.sql.session;

import org.apache.flink.util.AbstractID;
import org.apache.flink.util.StringUtils;

import java.nio.ByteBuffer;

/**
 * Unique (at least statistically unique) identifier for a Session.
 */
public final class SessionID extends AbstractID {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new (statistically) random SessionID.
	 */
	public SessionID() {
		super();
	}

	/**
	 * Creates a new SessionID, using the given lower and upper parts.
	 *
	 * @param lowerPart The lower 8 bytes of the ID.
	 * @param upperPart The upper 8 bytes of the ID.
	 */
	public SessionID(long lowerPart, long upperPart) {
		super(lowerPart, upperPart);
	}

	/**
	 * Creates a new SessionID from the given byte sequence. The byte sequence must be
	 * exactly 16 bytes long. The first eight bytes make up the lower part of the ID,
	 * while the next 8 bytes make up the upper part of the ID.
	 *
	 * @param bytes The byte sequence.
	 */
	public SessionID(byte[] bytes) {
		super(bytes);
	}

	// ------------------------------------------------------------------------
	//  Static factory methods
	// ------------------------------------------------------------------------

	/**
	 * Creates a new (statistically) random SessionID.
	 *
	 * @return A new random SessionID.
	 */
	public static SessionID generate() {
		return new SessionID();
	}

	/**
	 * Creates a new SessionID from the given byte sequence. The byte sequence must be
	 * exactly 16 bytes long. The first eight bytes make up the lower part of the ID,
	 * while the next 8 bytes make up the upper part of the ID.
	 *
	 * @param bytes The byte sequence.
	 * @return A new SessionID corresponding to the ID encoded in the bytes.
	 */
	public static SessionID fromByteArray(byte[] bytes) {
		return new SessionID(bytes);
	}

	public static SessionID fromByteBuffer(ByteBuffer buf) {
		long lower = buf.getLong();
		long upper = buf.getLong();
		return new SessionID(lower, upper);
	}

	/**
	 * Parses a SessionID from the given string.
	 *
	 * @param hexString string representation of a SessionID
	 * @return Parsed SessionID
	 * @throws IllegalArgumentException if the SessionID could not be parsed from the given string
	 */
	public static SessionID fromHexString(String hexString) {
		try {
			return new SessionID(StringUtils.hexStringToByte(hexString));
		} catch (Exception e) {
			throw new IllegalArgumentException(
				"Cannot parse SessionID from \"" + hexString + "\". The expected format is " +
					"[0-9a-fA-F]{32}, e.g. fd72014d4c864993a2e5a9287b4a9c5d.", e);
		}
	}
}

