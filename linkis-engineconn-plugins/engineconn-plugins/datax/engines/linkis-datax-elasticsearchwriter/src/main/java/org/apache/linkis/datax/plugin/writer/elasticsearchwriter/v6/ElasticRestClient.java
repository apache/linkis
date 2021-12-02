/*
 *
 *  Copyright 2020 WeBank
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.datax.plugin.writer.elasticsearchwriter.v6;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.Version;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.main.MainResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * @author davidhua
 * 2019/8/1
 */
public class ElasticRestClient {
    public static final Logger logger = LoggerFactory.getLogger(ElasticRestClient.class);

    private static final int HEAP_BUFFER_SIZE = 100 * 1024 * 1024;
    private static final int SOCK_TIMEOUT_IN_MILLISECONDS = 60000;
    private static final int CONN_TIMEOUT_IN_MILLISECONDS = 5000;
    private static final int REQ_TIMEOUT_IN_MILLISECONDS = 60000;
    private static final int MASTER_TIMEOUT_IN_MILLISECONDS = 30000;

    private static final String INCLUDE_TYPE_NAME = "include_type_name";
    private static final String MASTER_TIMEOUT = "master_timeout";

    static final String FIELD_PROPS = "properties";
    private static final String MAPPING_PATH = "_mapping";
    private static final String MAPPING_TYPE_HEAD = "_mapping_type";
    private static final int DEFAULT_BACKOFF_DELAY_MILLS = 1000;
    private static final int DEFAULT_BACKOFF_TIMES = 3;
    static final String MAPPING_TYPE_DEFAULT = "_doc";

    private static final RequestOptions COMMON_OPTIONS;

    private List<BulkProcessor> bulkProcessors = new ArrayList<>();
    private Map<String, Object> clientConfig = new HashMap<>();
    private boolean matchVerison = true;
    static{
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        builder.setHttpAsyncResponseConsumerFactory(
                new HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory(HEAP_BUFFER_SIZE)
        );
        COMMON_OPTIONS = builder.build();
    }
    private RestHighLevelClient restClient;

    ElasticRestClient(String[] endPoint, String username, String password, SSLContext sslContext,
                      Map<String, Object> clientConfig) throws IOException {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));
        initialClient(endPoint, credentialsProvider, sslContext, clientConfig);
    }

    ElasticRestClient(String[] endPoints, CredentialsProvider credentialsProvider,
                      SSLContext sslContext, Map<String, Object> clientConfig) throws IOException {
        initialClient(endPoints, credentialsProvider, sslContext, clientConfig);
    }

    BulkProcessor createBulk(BulkProcessor.Listener listener, int bulkActions, int bulkPerTask){
        BiConsumer<BulkRequest, ActionListener<BulkResponse>> consumer = ((bulkRequest, bulkResponseActionListener)
            -> restClient.bulkAsync(bulkRequest, COMMON_OPTIONS, bulkResponseActionListener));
        BulkProcessor.Builder builder = BulkProcessor.builder(consumer, listener);
        builder.setBulkActions(bulkActions);
        builder.setBulkSize(new ByteSizeValue(-1, ByteSizeUnit.BYTES));
        builder.setConcurrentRequests(bulkPerTask - 1);
        builder.setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueMillis(DEFAULT_BACKOFF_DELAY_MILLS),
                DEFAULT_BACKOFF_TIMES));
        BulkProcessor bulkProcessor = builder.build();
        bulkProcessors.add(bulkProcessor);
        return bulkProcessor;
    }

    void close(){
        for(BulkProcessor bulkProcessor : bulkProcessors){
            bulkProcessor.close();
        }
        execute(restClient ->{
            try {
                restClient.close();
            }catch(Exception e){
                throw DataXException.asDataXException(ElasticWriterErrorCode.CLOSE_EXCEPTION, e);
            }
            return null;
        });
    }

    boolean existIndices(String... indices){
        return execute(restClient -> restClient.indices().exists(configureTimedRequest(new GetIndexRequest(indices)),
                COMMON_OPTIONS));
    }

    boolean deleteIndices(String... indices){
        return execute( restClient -> {
                AcknowledgedResponse response = restClient.indices()
                        .delete(new DeleteIndexRequest(indices), COMMON_OPTIONS);
                return response.isAcknowledged();
        });
    }

    void createIndex(String indexName, String typeName, Map<String, Object> settings,
                     Map<Object, Object> properties){
        execute( restClient ->{
            if(!existIndices(indexName)) {
                createIndex(indexName, settings);
            }
            putMapping(indexName, typeName, properties);
            return null;
        });
    }

    Map<Object, Object> getProps(String indexName, String typeName){
        return execute( restClient->{
            GetMappingsRequest request = new GetMappingsRequest();
            request.indices(indexName);
            RequestOptions.Builder optionsBuilder = COMMON_OPTIONS.toBuilder();
            optionsBuilder.addHeader(MAPPING_TYPE_HEAD, typeName);
            GetMappingsResponse response = restClient.indices()
                    .getMapping(configureTimedRequest(request), optionsBuilder.build());
            Map<String, Object> typeMap = response.mappings().get(indexName).sourceAsMap();
            Map propsMap = typeMap;
            if(typeMap.containsKey(typeName)) {
                Object type = typeMap.get(typeName);
                if (type instanceof Map) {
                    propsMap = (Map)type;
                }
            }
            Object props = propsMap.get(FIELD_PROPS);
            if (props instanceof Map) {
                return (Map) props;
            }
            return null;
        });
    }
    private void putMapping(String indexName, String typeName, Map<Object, Object> properties) throws IOException {
        if(null == properties){
            properties = new HashMap<>();
        }
        Map<String, Object> mappings = new HashMap<>(1);
        mappings.put(FIELD_PROPS, properties);
        PutMappingRequest request = new PutMappingRequest(indexName).source(mappings);
        RequestOptions.Builder optionsBuilder = COMMON_OPTIONS.toBuilder();
        optionsBuilder.addHeader(MAPPING_TYPE_HEAD, typeName);
        AcknowledgedResponse acknowledgedResponse = restClient.indices().putMapping(configureTimedRequest(request), optionsBuilder.build());
        if(!acknowledgedResponse.isAcknowledged()){
            throw DataXException.asDataXException(ElasticWriterErrorCode.PUT_MAPPINGS_ERROR,
                    "can't put mapping, type:[" + typeName +"], properties:" +JSON.toJSONString(properties));
        }
    }

    private void createIndex(String indexName, Map<String, Object> settings) throws IOException {
        if(null == settings){
            settings = new HashMap<>(1);
        }
        CreateIndexRequest request = new CreateIndexRequest(indexName)
                .settings(settings).waitForActiveShards(ActiveShardCount.DEFAULT);
        try {
            CreateIndexResponse response = restClient.indices().create(configureTimedRequest(request), COMMON_OPTIONS);
            if(!response.isAcknowledged()){
                throw DataXException.asDataXException(ElasticWriterErrorCode.CREATE_INDEX_ERROR, "can't create index:[" + indexName +
                        "], settings:" + JSON.toJSONString(settings) + ", message:[acknowledged=false]");
            }
        }catch(ElasticsearchException e){
            if(e.status().getStatus()
                    != RestStatus.BAD_REQUEST.getStatus()){
                throw e;
            }
            logger.error("index:["+ indexName +"] maybe already existed, status=" + e.status().getStatus());
        }
    }

    private <T extends TimedRequest>T configureTimedRequest(T request){
        request.setMasterTimeout(TimeValue
                .timeValueMillis(Integer
                        .valueOf(String.valueOf(clientConfig.getOrDefault(ElasticKey.CLIENT_CONFIG_MASTER_TIMEOUT, MASTER_TIMEOUT_IN_MILLISECONDS)))
                ));
        request.setTimeout(TimeValue
                .timeValueMillis(Integer
                        .valueOf(String.valueOf(clientConfig.getOrDefault(ElasticKey.CLIENT_CONFIG_REQ_TIMEOUT, REQ_TIMEOUT_IN_MILLISECONDS)))
                ));
        return request;
    }

    private <R>R execute(Exec<RestHighLevelClient, R> execFunc){
        try {
            return execFunc.apply(restClient);
        }catch(ElasticsearchException e){
            throw DataXException.asDataXException(ElasticWriterErrorCode.REQUEST_ERROR, e.status().name(), e);
        }catch (Exception e) {
            throw DataXException.asDataXException(ElasticWriterErrorCode.BAD_CONNECT, e);
        }
    }


    static ElasticRestClient custom(String[] endPoints, Map<String, Object> clientConfig){
        try {
            return new ElasticRestClient(endPoints, null, null, clientConfig);
        } catch (IOException e) {
            throw DataXException.asDataXException(ElasticWriterErrorCode.BAD_CONNECT, e);
        }
    }

    static ElasticRestClient custom(String[] endPoints,
                                    String username, String password, Map<String, Object> clientConfig){
        try {
            return new ElasticRestClient(endPoints, username, password, null, clientConfig);
        } catch (IOException e) {
            throw DataXException.asDataXException(ElasticWriterErrorCode.BAD_CONNECT, e);
        }
    }

    static ElasticRestClient sslCustom(String[] endPoints,
                                              String keyStorePath, String keyStorePass, Map<String, Object> clientConfig){
        try {
            return new ElasticRestClient(endPoints, null, buildSSLContext(keyStorePath, keyStorePass)
            , clientConfig);
        } catch (IOException e) {
            throw DataXException.asDataXException(ElasticWriterErrorCode.BAD_CONNECT, e);
        }
    }

    static ElasticRestClient sslCustom(String[] endPoints,
                                              String username, String password,
                                              String keyStorePath, String keyStorePass, Map<String, Object> clientConfig){
        try{
            return new ElasticRestClient(endPoints, username, password,
                    buildSSLContext(keyStorePath, keyStorePass), clientConfig);
        }catch(IOException e){
            throw DataXException.asDataXException(ElasticWriterErrorCode.BAD_CONNECT, e);
        }
    }

    private static SSLContext buildSSLContext(String keyStorePath, String keyStorePass){
        try {
            KeyStore truststore = KeyStore.getInstance("jks");
            try (InputStream inputStream = Files.newInputStream(Paths.get(new URI(keyStorePath)))) {
                truststore.load(inputStream, keyStorePass.toCharArray());
            } catch (URISyntaxException | IOException | NoSuchAlgorithmException | CertificateException e) {
                throw DataXException.asDataXException(ElasticWriterErrorCode.BAD_CONNECT, e);
            }
            SSLContextBuilder sslContextBuilder = SSLContexts.custom()
                    .loadTrustMaterial(truststore, null);
            return sslContextBuilder.build();
        }catch(KeyStoreException | NoSuchAlgorithmException | KeyManagementException e){
            throw DataXException.asDataXException(ElasticWriterErrorCode.BAD_CONNECT, e);
        }
    }

    private void initialClient(String[] endPoints, CredentialsProvider credentialsProvider,
                               SSLContext sslContext, Map<String, Object> clientConfig) throws IOException {
        if(null == clientConfig){
            clientConfig = Collections.emptyMap();
        }
        HttpHost[] httpHosts = new HttpHost[endPoints.length];
        for(int i = 0 ; i < endPoints.length; i++){
            httpHosts[i] = HttpHost.create(endPoints[i]);
        }
        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);
        Map<String, Object> finalClientConfig = clientConfig;
        restClientBuilder.setHttpClientConfigCallback(
                httpClientBuilder -> {
                    if(null != credentialsProvider) {
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                    if(null != sslContext){
                        httpClientBuilder.setSSLContext(sslContext);
                    }
                    httpClientBuilder.addInterceptorFirst((HttpRequestInterceptor) (httpRequest, httpContext) -> {
                        if(httpRequest instanceof HttpRequestWrapper){
                            HttpRequestWrapper wrapper = (HttpRequestWrapper)httpRequest;
                            String uri = wrapper.getURI().toString();
                            if(matchVerison) {
                                uri = uri.replace(INCLUDE_TYPE_NAME + "=false", INCLUDE_TYPE_NAME + "=true");
                            }else{
                                //when use the different version, remove the INCLUDE_TYPE_NAME
                                uri = uri.replaceAll(INCLUDE_TYPE_NAME + "=[^&]+", "")
                                .replaceAll(MASTER_TIMEOUT + "=[^&]+", "");
                            }
                            String type = MAPPING_TYPE_DEFAULT;
                            if (null != wrapper.getFirstHeader(MAPPING_TYPE_HEAD)) {
                                type = wrapper.getFirstHeader(MAPPING_TYPE_HEAD).getValue();
                            }
                            uri = uri.replace(MAPPING_PATH, MAPPING_PATH + "/" + type);
                            try {
                                wrapper.setURI(new URI(uri));
                            } catch (URISyntaxException e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    });
                    httpClientBuilder.setMaxConnTotal(Integer.parseInt(
                            String.valueOf(finalClientConfig.getOrDefault(ElasticKey.CLIENT_CONFIG_POOL_SIZE, 1))));
                    return httpClientBuilder;
                }
        );
        restClientBuilder.setRequestConfigCallback(
                requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout(Integer.parseInt(
                                String.valueOf(finalClientConfig.getOrDefault(ElasticKey.CLIENT_CONFIG_CONN_TIMEOUT,
                                        CONN_TIMEOUT_IN_MILLISECONDS))))
                        .setConnectionRequestTimeout(Integer.parseInt(
                                String.valueOf(finalClientConfig.getOrDefault(ElasticKey.CLIENT_CONFIG_CONN_TIMEOUT,
                                        CONN_TIMEOUT_IN_MILLISECONDS))))
                        .setSocketTimeout(Integer.parseInt(
                                String.valueOf(finalClientConfig.getOrDefault(ElasticKey.CLIENT_CONFIG_SOCKET_TIMEOUT,
                                        SOCK_TIMEOUT_IN_MILLISECONDS)))));
        restClient = new RestHighLevelClient(restClientBuilder);
        boolean connect = restClient.ping(COMMON_OPTIONS);
        if(! connect){
           throw DataXException.asDataXException(ElasticWriterErrorCode.BAD_CONNECT, "Ping to elastic server failed");
        }
        //check the version
        checkVersion();
        this.clientConfig = clientConfig;
    }

    private void checkVersion() throws IOException {
        logger.info("Check the version of ElasticSearch");
        MainResponse response = restClient.info(COMMON_OPTIONS);
        Version version = response.getVersion();
        if(!version.isCompatible(Version.CURRENT)){
            throw DataXException.asDataXException(ElasticWriterErrorCode.CONFIG_ERROR,
                    "ElasticSearch's version is not compatible");
        }
        logger.info("The version of ElasticSearch: [" + version.toString() +"]");
        if(version.major != Version.CURRENT.major){
            throw DataXException.asDataXException(ElasticWriterErrorCode.CONFIG_ERROR,
                    "ElasticSearch's version is not compatible");
        }
    }
    @FunctionalInterface
    interface Exec<T, R> {
        R apply(T t) throws Exception;
    }

}
