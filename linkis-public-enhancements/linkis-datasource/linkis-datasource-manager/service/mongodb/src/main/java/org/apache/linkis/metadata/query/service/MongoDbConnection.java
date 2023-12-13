/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.metadata.query.service;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.metadata.query.common.domain.MetaColumnInfo;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mongodb.client.model.Sorts.descending;

public class MongoDbConnection implements Closeable {
  private static final Logger LOG = LoggerFactory.getLogger(MongoDbConnection.class);

  private static final CommonVars<Integer> CONNECTIONS_PER_HOST =
      CommonVars.apply("wds.linkis.server.mdm.service.mongo.driver", 5);

  private static final CommonVars<Integer> CONNECT_TIMEOUT =
      CommonVars.apply("wds.linkis.server.mdm.service.mongo.connect.timeout", 3000);

  private static final CommonVars<Integer> SOCKET_TIMEOUT =
      CommonVars.apply("wds.linkis.server.mdm.service.mongo.socket.timeout", 6000);

  private static final int DOC_FETCH_LIMIT = 100;

  private static final String DEFAULT_PRIMARY_KEY = "_id";

  /** Define the default options */
  private static final MongoClientOptions CLIENT_SERVICE_OPTIONS =
      MongoClientOptions.builder()
          .connectionsPerHost(5)
          .socketTimeout(6000)
          .connectTimeout(3000)
          .serverSelectionTimeout(50)
          .build();

  private MongoClient conn;

  private ConnectMessage connectMessage;

  public MongoDbConnection(
      String host,
      Integer port,
      String username,
      String password,
      String database,
      Map<String, Object> extraParams)
      throws ClassNotFoundException, Exception {
    connectMessage = new ConnectMessage(host, port, username, password, database, extraParams);
    conn = getDBConnection(connectMessage, database);
  }

  public List<String> getAllDatabases() throws Exception {
    LOG.info("start to get database");
    List<String> databases = new ArrayList<>();
    MongoIterable<String> databaseNamesIter = this.conn.listDatabaseNames();
    for (String databaseName : databaseNamesIter) {
      databases.add(databaseName);
    }
    return databases;
  }

  public List<String> getAllTables(String database) throws Exception {
    List<String> collectionNames = new ArrayList<>();
    MongoDatabase mdb = this.conn.getDatabase(database);
    MongoIterable<String> collections = mdb.listCollectionNames();
    for (String cname : collections) {
      collectionNames.add(cname);
    }
    return collectionNames;
  }

  public List<MetaColumnInfo> getColumns(String database, String collection)
      throws Exception, ClassNotFoundException {
    List<MetaColumnInfo> metaColumnInfo = new ArrayList<>();
    MongoDatabase mdb = this.conn.getDatabase(database);
    MongoCollection<Document> collection1 = mdb.getCollection(collection);
    MongoCursor<Document> dbCursor =
        collection1
            .find()
            .sort(descending("_id"))
            .batchSize(DOC_FETCH_LIMIT)
            .limit(DOC_FETCH_LIMIT)
            .iterator();
    int maxFieldSize = Integer.MIN_VALUE;
    Document maxDocument = null;
    while (dbCursor.hasNext()) {
      Document document = dbCursor.next();
      if (document.keySet().size() > maxFieldSize) {
        maxDocument = document;
        maxFieldSize = document.keySet().size();
      }
    }
    if (Objects.nonNull(maxDocument)) {
      AtomicInteger index = new AtomicInteger(0);
      maxDocument.forEach(
          (colName, colValue) -> {
            MetaColumnInfo info = new MetaColumnInfo();
            // Index
            info.setIndex(index.getAndIncrement());
            // Field name
            info.setName(colName);
            // Field type
            info.setType(Objects.nonNull(colValue) ? colValue.getClass().getSimpleName() : "Null");
            if (info.getName().equals(DEFAULT_PRIMARY_KEY)) {
              info.setPrimaryKey(true);
            }
            metaColumnInfo.add(info);
          });
    }
    return metaColumnInfo;
  }

  /**
   * Get primary keys
   *
   * @param connection connection
   * @return
   * @throws Exception
   */
  private List<String> getPrimaryKeys(MongoClient connection, String database, String collection)
      throws Exception {
    List<String> primaryKeys = new ArrayList<>();
    primaryKeys.add(DEFAULT_PRIMARY_KEY);
    return primaryKeys;
  }

  /**
   * close database resource
   *
   * @param connection connection
   */
  private void closeResource(MongoClient connection) {
    if (null != connection) {
      connection.close();
    }
  }

  @Override
  public void close() throws IOException {
    closeResource(conn);
  }

  /**
   * @param connectMessage
   * @param database
   * @return
   * @throws ClassNotFoundException
   */
  private MongoClient getDBConnection(ConnectMessage connectMessage, String database)
      throws Exception {

    LOG.info("mongo information is database:{}, username:{}", database, connectMessage.username);

    MongoClient client = null;
    try {
      MongoCredential credential =
          MongoCredential.createCredential(
              connectMessage.username, database, connectMessage.password.toCharArray());
      ServerAddress address = new ServerAddress(connectMessage.host, connectMessage.port);
      client = new MongoClient(address, credential, CLIENT_SERVICE_OPTIONS);
      MongoDatabase mdb = client.getDatabase(database);
      MongoIterable<String> collectionname = mdb.listCollectionNames();
      collectionname.first();
      return client;
      // return new MongoClient(address, credential, CLIENT_SERVICE_OPTIONS);
    } catch (MongoSocketException e) {
      throw new RuntimeException(
          "Fail in building socket connection to Mongo server: [ host: "
              + connectMessage.host
              + ", port: "
              + connectMessage.port
              + "]",
          e);
    } catch (MongoSecurityException e) {
      throw new RuntimeException(
          "Fail to authenticate to Mongo server: [ host: "
              + connectMessage.host
              + ", port: "
              + connectMessage.port
              + ", username: "
              + connectMessage.username
              + "]",
          e);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(e);
    } catch (Exception e) {
      LOG.error("exchange.mongodb.obtain.data_source.info.failed" + e.getMessage());
      throw new Exception();
    }
  }

  /** Connect message */
  private static class ConnectMessage {
    private String host;

    private Integer port;

    private String username;

    private String password;

    private String database;

    private Map<String, Object> extraParams;

    public ConnectMessage(
        String host,
        Integer port,
        String username,
        String password,
        String database,
        Map<String, Object> extraParams) {
      this.host = host;
      this.port = port;
      this.username = username;
      this.password = password;
      this.database = database;
      if (extraParams != null) {
        this.extraParams = extraParams;
      }
      this.extraParams = extraParams;
      this.extraParams.put("connectTimeout", CONNECT_TIMEOUT.getValue());
      this.extraParams.put("socketTimeout", SOCKET_TIMEOUT.getValue());
    }
  }
}
