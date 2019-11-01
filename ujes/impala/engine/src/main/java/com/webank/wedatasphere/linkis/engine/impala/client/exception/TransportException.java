package com.webank.wedatasphere.linkis.engine.impala.client.exception;

/**
 * 传输错误
 * 
 * @author dingqihuang
 * @version Sep 20, 2019
 */
public class TransportException extends Exception {

	private static final long serialVersionUID = 5045965784519089392L;

	public TransportException(String message) {
		super(message);
	}

	public TransportException(Exception exception) {
		super(exception);
	}

	public static TransportException of(ExceptionCode code) {
		return new TransportException(code.getMessage());
	}

	public static TransportException of(ExceptionCode code, String massage) {
		return new TransportException(String.format("%s: %s", massage, code.getMessage()));
	}

	public static TransportException of(Exception exception) {
		return new TransportException(exception);
	}
}
