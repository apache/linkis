package com.webank.wedatasphere.linkis.engine.impala.client.factory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.transport.TSaslClientTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.webank.wedatasphere.linkis.engine.impala.client.ImpalaClient;
import com.webank.wedatasphere.linkis.engine.impala.client.exception.TransportException;
import com.webank.wedatasphere.linkis.engine.impala.client.thrift.ImpalaThriftClientOnHiveServer2;
import com.webank.wedatasphere.linkis.engine.impala.client.util.Constant;

/**
 * 客户端工厂
 * 
 * @author dingqihuang
 * @version Sep 20, 2019
 */
public class ImpalaClientFactory {

	public enum Protocol {
		Thrift;
	}

	public enum Transport {
		HiveServer2;
	}

	public static ClientBuilder builder() {
		return new ClientBuilder();
	}

	public static class ClientBuilder {
		private String host;
		private int port;
		private Protocol protocol = Protocol.Thrift;
		private Transport transport = Transport.HiveServer2;

		private String username;
		private String password;
		private boolean useTicket = false;
		private String ticketBin = Constant.DEFAULT_LOGIN_TICKET_BIN;

		private boolean useSsl;
		private String sslType = Constant.DEFAULT_SSL_TYPE;
		private String trustStoreType = Constant.DEFAULT_TRUSTSTORE_TYPE;

		private boolean trustAll = false;
		private String trustStore;
		private String trustStorePassword;

		private int parallelLimit = Constant.DEFAULT_PARALLEL_LIMIT;
		private int heartBeatsInSecond = Constant.DEFAULT_HEART_BEAT_IN_SECOND;

		private int connectionTimeout = 10;

		private String defaultQueueName = "root.default";
		private int batchSize;

		private ClientBuilder() {
		}

		/**
		 * 调用协议
		 * 
		 * @param protocol
		 *            调用协议, 默认thrift
		 * @return
		 */
		public ClientBuilder withProtocol(Protocol protocol) {
			this.protocol = protocol;
			return this;
		}

		/**
		 * 传输协议
		 * 
		 * @param transport
		 *            传输协议, 默认hiveServer2
		 * @return
		 */
		public ClientBuilder withTransport(Transport transport) {
			this.transport = transport;
			return this;
		}

		/**
		 * 通信节点
		 * 
		 * @param host
		 *            地址
		 * @param port
		 *            端口
		 * @return
		 */
		public ClientBuilder withCoordinator(String host, int port) {
			this.host = host;
			this.port = port;
			return this;
		}

		/**
		 * 通信节点
		 * 
		 * @param host
		 *            地址
		 * @param port
		 *            端口
		 * @return
		 * @see ClientBuilder.withCoordinator
		 */
		public ClientBuilder withHost(String host, int port) {
			return withCoordinator(host, port);
		}

		/**
		 * 指定用户名
		 * 
		 * @param username
		 * @return
		 */
		public ClientBuilder withUsername(String username) {
			this.username = username;
			return this;
		}

		/**
		 * 指定密码
		 * 
		 * @param password
		 * @return
		 */
		public ClientBuilder withPassword(String password) {
			this.password = password;
			return this;
		}

		/**
		 * 指定用户名和密码
		 * 
		 * @param username
		 * @param password
		 * @return
		 */
		public ClientBuilder withCredential(String username, String password) {
			this.username = username;
			this.password = password;
			return this;
		}

		/**
		 * 是否启用动态密码
		 * 
		 * @param useTicket
		 * @return
		 */
		public ClientBuilder withLoginTicket(boolean useTicket) {
			this.useTicket = useTicket;
			return this;
		}

		/**
		 * 指定密码生成器
		 * 
		 * @param ticketBin
		 *            生成器命令
		 * @return
		 */
		public ClientBuilder withLoginTicket(String ticketBin) {
			withLoginTicket(true);
			this.ticketBin = ticketBin;
			return this;
		}

		/**
		 * 设置SSL类型
		 * 
		 * @param sslContextType
		 *            类型, 默认TSL
		 * @return
		 */
		public ClientBuilder withSSL(String sslContextType) {
			withSSL(true);
			this.sslType = sslContextType;
			return this;
		}

		/**
		 * 启用SSL
		 * 
		 * @param useSsl
		 * @return
		 */
		public ClientBuilder withSSL(boolean useSsl) {
			this.useSsl = useSsl;
			return this;
		}

		/**
		 * 信任证书
		 * 
		 * @param filePath
		 *            keyStore文件
		 * @param password
		 *            密码
		 * @param fileType
		 *            类型, 默认jks
		 * @return
		 */
		public ClientBuilder withTrustStore(String filePath, String password, String fileType) {
			withTrustStore(filePath, password);
			this.trustStoreType = fileType;
			return this;
		}

		/**
		 * 信任证书
		 * 
		 * @param filePath
		 *            keyStore文件
		 * @param password
		 *            密码
		 * @return
		 */
		public ClientBuilder withTrustStore(String filePath, String password) {
			this.trustAll = false;
			this.trustStore = filePath;
			this.trustStorePassword = password;
			return this;
		}

		/**
		 * 是否信任所有证书
		 * 
		 * @param trustAll
		 * @return
		 */
		public ClientBuilder trustAllCertification(boolean trustAll) {
			this.trustAll = trustAll;
			return this;
		}

		/**
		 * 并行限制
		 * 
		 * @param parallelLimit
		 * @return
		 */
		public ClientBuilder withParallelLimit(int parallelLimit) {
			this.parallelLimit = parallelLimit;
			return this;
		}

		/**
		 * 轮询间隔（秒）
		 * 
		 * @param heartBeatsInSecond
		 * @return
		 */
		public ClientBuilder withHeartBeatsInSecond(int heartBeatsInSecond) {
			this.heartBeatsInSecond = heartBeatsInSecond;
			return this;
		}

		/**
		 * 连接超时时间（秒）
		 * 
		 * @param connectionTimeout
		 * @return
		 */
		public ClientBuilder withConnectionTimeout(int connectionTimeout) {
			this.connectionTimeout = connectionTimeout;
			return this;
		}

		/**
		 * 设置提交队列
		 * 
		 * @param queueName
		 * @return
		 */
		public ClientBuilder withSubmitQueue(String queueName) {
			this.defaultQueueName = queueName;
			return this;
		}

		/**
		 * 并行限制
		 * 
		 * @param parallelLimit
		 * @return
		 */
		public ClientBuilder withBatchSize(int batchSize) {
			this.batchSize = batchSize;
			return this;
		}

		@SuppressWarnings("resource")
		public ImpalaClient build() throws NoSuchAlgorithmException, CertificateException, FileNotFoundException,
				IOException, KeyStoreException, KeyManagementException, TransportException {
			SocketFactory socketFactory = null;
			if (useSsl) {
				SSLContext context = SSLContext.getInstance(sslType);
				KeyStore keyStore = KeyStore.getInstance(trustStoreType);

				TrustManager[] trustManagers = null;
				if (trustAll) {
					trustManagers = trustAllCerts;
				} else if (StringUtils.isNotBlank(trustStore)) {
					/*
					 * 读取信任证书
					 */
					keyStore.load(new FileInputStream(trustStore), trustStorePassword.toCharArray());
					TrustManagerFactory trustManagerFactory = TrustManagerFactory
							.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					trustManagerFactory.init(keyStore);
					trustManagers = trustManagerFactory.getTrustManagers();
				}
				context.init(null, trustManagers, new SecureRandom());

				socketFactory = context.getSocketFactory();
			} else {
				socketFactory = SocketFactory.getDefault();
			}

			Socket socket = socketFactory.createSocket(host, port);
			TTransport tTransport = null;
			try {
				TSocket tSocket = new TSocket(socket);
				tSocket.setConnectTimeout(connectionTimeout * 1000);

				tTransport = tSocket;

				/*
				 * 生成动态密码
				 */
				if (useTicket) {
					username = System.getProperty("user.name");
					try {
						Process process = new ProcessBuilder().command(ticketBin).start();
						int ret = process.waitFor();
						if (ret != 0) {
							throw new RuntimeException(
									IOUtils.toString(process.getErrorStream(), Constant.DEFAULT_CHARSET));
						}
						password = IOUtils.toString(process.getInputStream(), Constant.DEFAULT_CHARSET);
					} catch (RuntimeException e) {
						throw e;
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}

				if (StringUtils.isNotBlank(password)) {
					CallbackHandler callbackHandler = new CallbackHandler() {
						@Override
						public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
							for (Callback callback : callbacks) {
								if (callback instanceof NameCallback) {
									((NameCallback) callback).setName(username);
									;
								} else if (callback instanceof PasswordCallback) {
									((PasswordCallback) callback).setPassword(password.toCharArray());
								}
							}
						}
					};

					/*
					 * SASL+LDAP认证
					 */
					tTransport = new TSaslClientTransport("PLAIN", (String) null, "LDAP", host, null, callbackHandler,
							tTransport);
				}

			} catch (TTransportException e) {
				throw new RuntimeException(e);
			}

			ImpalaClient impalaClient = null;

			/*
			 * Thrift + HiveServer2
			 */
			if (protocol == Protocol.Thrift && transport == Transport.HiveServer2) {
				impalaClient = new ImpalaThriftClientOnHiveServer2(tTransport, parallelLimit, heartBeatsInSecond);
			}

			if (impalaClient != null) {
				impalaClient.setRequestPool(defaultQueueName);
				if (batchSize > 0) {
					impalaClient.setBatchSize(batchSize);
				}
				return impalaClient;
			}

			throw new RuntimeException("Unknown client type");
		}
	}

	private static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	} };
}
