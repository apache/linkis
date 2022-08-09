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

package org.apache.linkis.metadata.query.common.service;

import org.apache.linkis.common.exception.WarnException;
import org.apache.linkis.datasourcemanager.common.util.json.Json;
import org.apache.linkis.metadata.query.common.cache.CacheConfiguration;
import org.apache.linkis.metadata.query.common.cache.CacheManager;
import org.apache.linkis.metadata.query.common.cache.ConnCacheManager;
import org.apache.linkis.metadata.query.common.domain.MetaColumnInfo;
import org.apache.linkis.metadata.query.common.domain.MetaPartitionInfo;
import org.apache.linkis.metadata.query.common.exception.MetaRuntimeException;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;

import com.google.common.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public abstract class AbstractMetaService<C extends Closeable> implements MetadataService {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractMetaService.class);
    private static final String CONN_CACHE_REQ = "_STORED";

    private CacheManager connCacheManager;

    /** Caching connections which built by connect parameters requested with */
    protected Cache<String, MetadataConnection<C>> reqCache;

    @PostConstruct
    public void init() {
        connCacheManager = ConnCacheManager.custom();
        initCache(connCacheManager);
    }
    /**
     * If want to use cache component, you should invoke this in constructor method
     *
     * @param cacheManager
     */
    protected void initCache(CacheManager cacheManager) {
        String prefix = this.getClass().getSimpleName();
        reqCache =
                cacheManager.buildCache(
                        prefix + CONN_CACHE_REQ,
                        notification -> {
                            assert notification.getValue() != null;
                            close(notification.getValue().getConnection());
                        });
        // Clean up the req cache
        reqCache.cleanUp();
    }

    @Override
    public abstract MetadataConnection<C> getConnection(String operator, Map<String, Object> params)
            throws Exception;

    @Override
    public List<String> getDatabases(String operator, Map<String, Object> params) {
        return this.getConnAndRun(operator, params, this::queryDatabases);
    }

    @Override
    public List<String> getTables(String operator, Map<String, Object> params, String database) {
        return this.getConnAndRun(operator, params, conn -> this.queryTables(conn, database));
    }

    @Override
    public Map<String, String> getTableProps(
            String operator, Map<String, Object> params, String database, String table) {
        return this.getConnAndRun(
                operator, params, conn -> this.queryTableProps(conn, database, table));
    }

    @Override
    public MetaPartitionInfo getPartitions(
            String operator,
            Map<String, Object> params,
            String database,
            String table,
            boolean traverse) {
        return this.getConnAndRun(
                operator, params, conn -> this.queryPartitions(conn, database, table, traverse));
    }

    @Override
    public List<MetaColumnInfo> getColumns(
            String operator, Map<String, Object> params, String database, String table) {
        return this.getConnAndRun(
                operator, params, conn -> this.queryColumns(conn, database, table));
    }

    @Override
    public Map<String, String> getPartitionProps(
            String operator,
            Map<String, Object> params,
            String database,
            String table,
            String partition) {
        return this.getConnAndRun(
                operator,
                params,
                conn -> this.queryPartitionProps(conn, database, table, partition));
    }

    /**
     * Get database list by connection
     *
     * @param connection metadata connection
     * @return
     */
    public List<String> queryDatabases(C connection) {
        throw new WarnException(-1, "This method is no supported");
    }

    /**
     * Get table list by connection and database
     *
     * @param connection metadata connection
     * @param database database
     * @return
     */
    public List<String> queryTables(C connection, String database) {
        throw new WarnException(-1, "This method is no supported");
    }

    /**
     * Get partitions by connection, database and table
     *
     * @param connection metadata connection
     * @param database database
     * @param table table
     * @return
     */
    public MetaPartitionInfo queryPartitions(
            C connection, String database, String table, boolean traverse) {
        throw new WarnException(-1, "This method is no supported");
    }

    /**
     * Get columns by connection, database and table
     *
     * @param connection metadata connection
     * @param database database
     * @param table table
     * @return
     */
    public List<MetaColumnInfo> queryColumns(C connection, String database, String table) {
        throw new WarnException(-1, "This method is no supported");
    }

    /**
     * Get the properties of partition
     *
     * @param connection
     * @param database
     * @param table
     * @param partition
     * @return
     */
    public Map<String, String> queryPartitionProps(
            C connection, String database, String table, String partition) {
        throw new WarnException(-1, "This method is no supported");
    }

    /**
     * Get table properties
     *
     * @param connection metadata connection
     * @param database database
     * @param table table
     * @return
     */
    public Map<String, String> queryTableProps(C connection, String database, String table) {
        throw new WarnException(-1, "This method is no supported");
    }

    public void close(C connection) {
        try {
            connection.close();
        } catch (IOException e) {
            throw new MetaRuntimeException(
                    "Fail to close connection[关闭连接失败], [" + e.getMessage() + "]", e);
        }
    }

    protected <R> R getConnAndRun(
            String operator, Map<String, Object> params, Function<C, R> action) {
        String cacheKey = "";
        MetadataConnection<C> connection = null;
        try {
            cacheKey = md5String(Json.toJson(params, null), "", 2);
            if (null != reqCache) {
                ConnectionCache<C> connectionCache =
                        getConnectionInCache(
                                reqCache, cacheKey, () -> getConnection(operator, params));
                connection = connectionCache.connection;
                // Update the actually cache key
                cacheKey = connectionCache.cacheKey;
            } else {
                connection = getConnection(operator, params);
            }
            return run(connection, action);
        } catch (Exception e) {
            LOG.error("Error to invoke meta service", e);
            if (StringUtils.isNotBlank(cacheKey) && Objects.nonNull(reqCache)) {
                reqCache.invalidate(cacheKey);
            }
            throw new MetaRuntimeException(e.getMessage(), e);
        } finally {
            if (Objects.nonNull(connection)
                    && connection.isLock()
                    && connection.getLock().isHeldByCurrentThread()) {
                connection.getLock().unlock();
            }
        }
    }

    private <R> R run(MetadataConnection<C> connection, Function<C, R> action) {
        if (connection.isLock()) {
            if (!connection.getLock().isHeldByCurrentThread()) {
                connection.getLock().lock();
                try {
                    return action.apply(connection.getConnection());
                } finally {
                    connection.getLock().unlock();
                }
            } else {
                return action.apply(connection.getConnection());
            }
        } else {
            return action.apply(connection.getConnection());
        }
    }

    /**
     * Get connection cache element
     *
     * @param cache cache entity
     * @param cacheKey cache key
     * @param callable callable function
     * @return connection cache
     * @throws ExecutionException exception in caching
     */
    private ConnectionCache<C> getConnectionInCache(
            Cache<String, MetadataConnection<C>> cache,
            String cacheKey,
            Callable<? extends MetadataConnection<C>> callable)
            throws ExecutionException {
        int poolSize = CacheConfiguration.CACHE_IN_POOL_SIZE.getValue();
        if (poolSize <= 0) {
            poolSize = 1;
        }
        MetadataConnection<C> connection = null;
        String cacheKeyInPool = cacheKey + "_0";
        for (int i = 0; i < poolSize; i++) {
            connection = cache.get(cacheKeyInPool, callable);
            if (!connection.isLock() || connection.getLock().tryLock()) {
                break;
            }
            cacheKeyInPool = cacheKey + "_" + i;
            LOG.info(
                    "The connection cache: ["
                            + cacheKeyInPool
                            + "] has been occupied, now to find the other in pool");
        }
        return new ConnectionCache<>(cacheKeyInPool, connection);
    }

    private String md5String(String source, String salt, int iterator) {
        StringBuilder token = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");
            if (StringUtils.isNotEmpty(salt)) {
                digest.update(salt.getBytes(StandardCharsets.UTF_8));
            }
            byte[] result = digest.digest(source.getBytes());
            for (int i = 0; i < iterator - 1; i++) {
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
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return token.toString();
    }

    /** Cache element */
    private static class ConnectionCache<C> {

        public ConnectionCache(String cacheKey, MetadataConnection<C> connection) {
            this.cacheKey = cacheKey;
            this.connection = connection;
        }
        /** Connection */
        MetadataConnection<C> connection;

        /** Actual cacheKey */
        String cacheKey;
    }
}
