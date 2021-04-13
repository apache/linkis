/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.engine.impala.client.exception;

/**
 *
 * Created by dingqihuang on Sep 20, 2019
 *
 */
public class TransportException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5045965784519089392L;

	public TransportException(String message) {
		super(message);
	}

	public TransportException(String message, Exception exception) {
		super(message, exception);
	}

	public static TransportException of(ExceptionCode code) {
		return new TransportException(code.getMessage());
	}
	
	public static TransportException of(ExceptionCode code, String massage) {
		return new TransportException(String.format("%s: %s", massage, code.getMessage()));
	}
	
	public static TransportException of(ExceptionCode code, Exception exception) {
		return new TransportException(code.getMessage(), exception);
	}
}
