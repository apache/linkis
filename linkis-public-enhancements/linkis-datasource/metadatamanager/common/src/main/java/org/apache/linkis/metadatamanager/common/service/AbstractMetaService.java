/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.metadatamanager.common.service;

import com.google.common.cache.Cache;
import org.apache.linkis.common.exception.WarnException;
import org.apache.linkis.metadatamanager.common.Json;
import org.apache.linkis.metadatamanager.common.cache.CacheManager;
import org.apache.linkis.metadatamanager.common.cache.ConnCacheManager;
import org.apache.linkis.metadatamanager.common.domain.MetaColumnInfo;
import org.apache.linkis.metadatamanager.common.domain.MetaPartitionInfo;
import org.apache.linkis.metadatamanager.common.exception.MetaRuntimeException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractMetaService<C extends Closeable> implements MetadataService {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMetaService.class);
    private static final String CONN_CACHE_REQ = "_STORED";


    private CacheManager connCacheManager;

    /**
     * Caching connections which built by connect parameters requested with
     */
    protected Cache<String, MetadataConnection<C>> reqCache;

    @PostConstruct
    public void init(){
        connCacheManager = ConnCacheManager.custom();
        initCache(connCacheManager);
    }
    /**
     * If want to use cache component, you should invoke this in constructor method
     * @param cacheManager
     */
    protected void initCache(CacheManager cacheManager){
        String prefix = this.getClass().getSimpleName();
        reqCache = cacheManager.buildCache(prefix + CONN_CACHE_REQ, notification ->{
            assert notification.getValue() != null;
            close(notification.getValue().getConnection());
        });
    }

    @Override
    public abstract MetadataConnection<C> getConnection(String operator, Map<String, Object> params) throws Exception;

    @Override
    public List<String> getDatabases(String operator, Map<String, Object> params) {
        return this.getConnAndRun(operator, params, this::queryDatabases);
    }

    @Override
    public List<String> getTables(String operator, Map<String, Object> params, String database) {
        return this.getConnAndRun(operator, params, conn -> this.queryTables(conn, database));
    }

    @Override
    public Map<String, String> getTableProps(String operator, Map<String, Object> params, String database, String table) {
        return this.getConnAndRun(operator, params, conn -> this.queryTableProps(conn, database, table));
    }

    @Override
    public MetaPartitionInfo getPartitions(String operator, Map<String, Object> params, String database, String table) {
        return this.getConnAndRun(operator, params, conn -> this.queryPartitions(conn, database, table));
    }

    @Override
    public List<MetaColumnInfo> getColumns(String operator, Map<String, Object> params, String database, String table) {
        return this.getConnAndRun(operator, params, conn -> this.queryColumns(conn, database, table));
    }

    /**
     * Get database list by connection
     * @param connection metadata connection
     * @return
     */
    public List<String> queryDatabases(C connection){
        throw new WarnException(-1, "This method is no supported");
    }

    /**
     * Get table list by connection and database
     * @param connection metadata connection
     * @param database database
     * @return
     */
    public List<String> queryTables(C connection, String database){
        throw new WarnException(-1, "This method is no supported");
    }

    /**
     * Get partitions by connection, database and table
     * @param connection metadata connection
     * @param database database
     * @param table table
     * @return
     */
    public MetaPartitionInfo queryPartitions(C connection, String database, String table){
        throw new WarnException(-1, "This method is no supported");
    }

    /**
     * Get columns by connection, database and table
     * @param connection metadata connection
     * @param database database
     * @param table table
     * @return
     */
    public List<MetaColumnInfo> queryColumns(C connection, String database, String table){
        throw new WarnException(-1, "This method is no supported");
    }

    /**
     * Get table properties
     * @param connection metadata connection
     * @param database database
     * @param table table
     * @return
     */
    public Map<String, String> queryTableProps(C connection, String database, String table){
        throw new WarnException(-1, "This method is no supported");
    }

    protected void close(C connection){
        try {
            connection.close();
        } catch (IOException e) {
            throw new MetaRuntimeException("Fail to close connection[关闭连接失败], [" + e.getMessage() + "]");
        }
    }

    protected <R>R getConnAndRun(String operator, Map<String, Object> params, Function<C, R> action) {
        String cacheKey = "";
        try{
            cacheKey = md5String(Json.toJson(params, null), "", 2);
            MetadataConnection<C> connection;
            if(null != reqCache) {
                connection = reqCache.get(cacheKey, () -> getConnection(operator, params));
            }else{
                connection = getConnection(operator, params);
            }
            return run(connection, action);
        }catch(Exception e){
            LOG.error("Error to invoke meta service", e);
            if(StringUtils.isNotBlank(cacheKey)){
                reqCache.invalidate(cacheKey);
            }
            throw new MetaRuntimeException(e.getMessage());
        }
    }
    private <R>R run(MetadataConnection<C> connection, Function<C, R> action){
        if(connection.isLock()){
            connection.getLock().lock();
            try{
                return action.apply(connection.getConnection());
            }finally{
                connection.getLock().unlock();
            }
        }else{
            return action.apply(connection.getConnection());
        }
    }

    private String md5String(String source, String salt, int iterator){
        StringBuilder token = new StringBuilder();
        try{
            MessageDigest digest = MessageDigest.getInstance("md5");
            if(StringUtils.isNotEmpty(salt)){
                digest.update(salt.getBytes(StandardCharsets.UTF_8));
            }
            byte[] result = digest.digest(source.getBytes());
            for(int i = 0; i < iterator - 1; i++){
                digest.reset();
                result = digest.digest(result);
            }
            for (byte aResult : result) {
                int temp = aResult & 0xFF;
                if (temp <= 0xF) {
                    token.append("0");
                }
                token.append(Integer.toHexString(temp));
            }
        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
        return token.toString();
    }
}
