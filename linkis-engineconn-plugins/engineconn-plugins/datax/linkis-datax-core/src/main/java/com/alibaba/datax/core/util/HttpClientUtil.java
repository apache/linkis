package com.alibaba.datax.core.util;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.util.RetryUtil;
import com.webank.wedatasphere.linkis.datax.common.GsonUtil;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @author davidhua
 */
public class HttpClientUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    private static CredentialsProvider provider;

    private CloseableHttpClient httpClient;

    private volatile static HttpClientUtil clientUtil;

    private static int HTTP_TIMEOUT_IN_MILLISECONDS = 5000;

    private static final int POOL_SIZE = 20;

    private static ThreadPoolExecutor asyncExecutor = RetryUtil.createThreadPoolExecutor();

    public static void setHttpTimeoutInMillionSeconds(int httpTimeoutInMillionSeconds) {
        HTTP_TIMEOUT_IN_MILLISECONDS = httpTimeoutInMillionSeconds;
    }

    public static synchronized HttpClientUtil getHttpClientUtil() {
        if (null == clientUtil) {
            synchronized (HttpClientUtil.class) {
                if (null == clientUtil) {
                    clientUtil = new HttpClientUtil();
                }
            }
        }
        return clientUtil;
    }

    public HttpClientUtil() {
        initApacheHttpClient();
    }

    public void destroy() {
        destroyApacheHttpClient();
    }

    public static void setBasicAuth(String username, String password) {
        Properties prob = SecretUtil.getSecurityProperties();
        provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(prob.getProperty("auth.user"), prob.getProperty("auth.pass")));
    }

    private void initApacheHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(HTTP_TIMEOUT_IN_MILLISECONDS)
                .setConnectTimeout(HTTP_TIMEOUT_IN_MILLISECONDS).setConnectionRequestTimeout(HTTP_TIMEOUT_IN_MILLISECONDS)
                .setStaleConnectionCheckEnabled(true).build();

        if (null == provider) {
            httpClient = HttpClientBuilder.create().setMaxConnTotal(POOL_SIZE).setMaxConnPerRoute(POOL_SIZE)
                    .setDefaultRequestConfig(requestConfig).build();
        } else {
            httpClient = HttpClientBuilder.create().setMaxConnTotal(POOL_SIZE).setMaxConnPerRoute(POOL_SIZE)
                    .setDefaultRequestConfig(requestConfig).setDefaultCredentialsProvider(provider).build();
        }
    }

    private void destroyApacheHttpClient() {
        try {
            if (httpClient != null) {
                httpClient.close();
                httpClient = null;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static HttpGet getGetRequest() {
        return new HttpGet();
    }

    public static HttpPost getPostRequest(String uri, HttpEntity entity, String... headers) {
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setEntity(entity);
        if(headers.length % 2 == 0){
            for(int i = 0; i < headers.length; i++){
                httpPost.addHeader(headers[i], headers[++i]);
            }
        }
        return httpPost;
    }

    public static HttpPut getPutRequest() {
        return new HttpPut();
    }

    public static HttpDelete getDeleteRequest() {
        return new HttpDelete();
    }

    public <T>T executeAndGet(HttpRequestBase httpRequestBase, Class<T> type) throws Exception {
        return httpClient.execute(httpRequestBase, httpResponse -> {
            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.info("Request path: " + httpRequestBase.getURI() + ", method：" + httpRequestBase.getMethod()
                        + ",STATUS CODE = " + httpResponse.getStatusLine().getStatusCode());
                httpRequestBase.abort();
                throw new RuntimeException("Response Status Code : " + httpResponse.getStatusLine().getStatusCode());
            } else {
                HttpEntity entity = httpResponse.getEntity();
                if (entity != null) {
                    String entityString =  EntityUtils.toString(entity, Consts.UTF_8);
                    if(type.equals(String.class)){
                        return (T)entityString;
                    }
                    return GsonUtil.fromJson(entityString, type);
                } else {
                    throw new RuntimeException("Response Entity Is Null");
                }
            }
        });
    }

    public <T>T executeAndGetWithRetry(final HttpRequestBase httpRequestBase,
                                         Class<T> type, final int retryTimes, final long retryInterval) {
        try {
            return RetryUtil.asyncExecuteWithRetry(() -> executeAndGet(httpRequestBase, type),
                    retryTimes, retryInterval, true, HTTP_TIMEOUT_IN_MILLISECONDS + 1000L, asyncExecutor);
        } catch (Exception e) {
            throw DataXException.asDataXException(FrameworkErrorCode.RUNTIME_ERROR, e);
        }
    }

}
