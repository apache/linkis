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

import com.alibaba.datax.common.element.Record;
import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.plugin.BasicDataReceiver;
import com.alibaba.datax.common.plugin.RecordReceiver;
import com.alibaba.datax.common.plugin.TaskPluginCollector;
import com.alibaba.datax.common.spi.Writer;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.statistics.plugin.task.util.DirtyRecord;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.linkis.datax.common.CryptoUtils;
import org.apache.linkis.datax.plugin.writer.elasticsearchwriter.v6.column.ElasticColumn;
import org.apache.linkis.datax.plugin.writer.elasticsearchwriter.v6.column.ElasticFieldDataType;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.apache.linkis.datax.plugin.writer.elasticsearchwriter.v6.ElasticWriter.Job.DEFAULT_ENDPOINT_SPLIT;
import static org.apache.linkis.datax.plugin.writer.elasticsearchwriter.v6.ElasticWriter.Job.WRITE_SIZE;

/**
 * @author davidhua
 * 2019/8/15
 */
public class ElasticWriter extends Writer {

    public static class Job extends Writer.Job{
        private static final Logger log = LoggerFactory.getLogger(Job.class);

        private static final String DEFAULT_ID = "_id";
        static final String WRITE_SIZE = "WRITE_SIZE";

        static final String DEFAULT_ENDPOINT_SPLIT = ",";

        private Configuration jobConf = null;
        private String[] endPoints;
        private String userName;
        private String password;

        @Override
        public void init() {
            this.jobConf = super.getPluginJobConf();
            this.validateParams();
        }
        @Override
        public void prepare() {
            ElasticRestClient restClient;
            Map<String, Object> clientConfig = jobConf.getMap(ElasticKey.CLIENT_CONFIG);
            if(StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)){
                restClient = ElasticRestClient.custom(endPoints, userName,
                        password, clientConfig);
            }else{
                restClient = ElasticRestClient.custom(endPoints, clientConfig);
            }
            String indexName = this.jobConf.getNecessaryValue(ElasticKey.INDEX_NAME, ElasticWriterErrorCode.REQUIRE_VALUE);
            String indexType = this.jobConf.getString(ElasticKey.INDEX_TYPE, "");
            String columnNameSeparator = this.jobConf.getString(ElasticKey.COLUMN_NAME_SEPARATOR, ElasticColumn.DEFAULT_NAME_SPLIT);
            List<Object> rawColumnList = jobConf
                    .getList(ElasticKey.PROPS_COLUMN);
            List<ElasticColumn> resolvedColumnList = new ArrayList<>();
            Map<Object, Object> props = resolveColumn(restClient, indexName, indexType,
                    rawColumnList, resolvedColumnList, columnNameSeparator);
            this.jobConf.set(ElasticKey.PROPS_COLUMN, resolvedColumnList);
            //clean up
            if(jobConf.getBool(ElasticKey.CLEANUP, false) &&
                    restClient.existIndices(indexName)){
                if(!restClient.deleteIndices(indexName)){
                    throw DataXException.asDataXException(ElasticWriterErrorCode.DELETE_INDEX_ERROR, "cannot delete index:[" + indexName +"]");
                }
            }
            //if the index is not existed, create it
            restClient.createIndex(indexName, indexType, jobConf.getMap(ElasticKey.SETTINGS),
                    props);
            restClient.close();
        }

        @Override
        public List<Configuration> split(int mandatoryNumber) {
            List<Configuration> configurations = new ArrayList<>();
            for( int i = 0; i < mandatoryNumber; i++){
                configurations.add(this.jobConf.clone());
            }
            return configurations;
        }


        @Override
        public void destroy() {

        }

        private void validateParams(){
            String endPoints = this.jobConf.getString(ElasticKey.ENDPOINTS);
            if(StringUtils.isBlank(endPoints)){
                throw DataXException.asDataXException(ElasticWriterErrorCode.REQUIRE_VALUE, "'endPoints(elasticUrls)' is necessary");
            }
            this.endPoints = endPoints.split(DEFAULT_ENDPOINT_SPLIT);
            this.userName = this.jobConf.getString(ElasticKey.USERNAME, "");
            this.password = this.jobConf.getString(ElasticKey.PASSWORD, "");
            if(StringUtils.isNotBlank(this.password)){
                try {
                    this.password = (String) CryptoUtils.string2Object(this.password);
                } catch (Exception e) {
                    throw DataXException.asDataXException(ElasticWriterErrorCode.CONFIG_ERROR, "decrypt password failed");
                }
            }
            this.jobConf.getNecessaryValue(ElasticKey.INDEX_NAME, ElasticWriterErrorCode.REQUIRE_VALUE);
        }

        private Map<Object, Object> resolveColumn(ElasticRestClient client,
                                                  String index, String type ,
                                                  List<Object> rawColumnList, List<ElasticColumn> outputColumn,
                                                  String columnNameSeparator){
            Map<Object, Object> properties;
            if(null != rawColumnList && !rawColumnList.isEmpty()) {
                //allow to custom the fields of properties
                properties = new HashMap<>(rawColumnList.size());
                rawColumnList.forEach(columnRaw -> {
                    String raw = columnRaw.toString();
                    ElasticColumn column = JSONObject
                            .parseObject(raw, ElasticColumn.class);
                    if (StringUtils.isNotBlank(column.getName()) && StringUtils.isNotBlank(column.getType())) {
                        outputColumn.add(column);
                        if (!column.getName().equals(DEFAULT_ID) && ElasticFieldDataType.valueOf(column.getType().toUpperCase())
                                != ElasticFieldDataType.ALIAS) {
                            Map property = JSONObject.parseObject(raw, Map.class);
                            property.remove(ElasticKey.PROPS_COLUMN_NAME);
                            properties.put(column.getName(), property);
                        }
                    }
                });
            }else{
                if(!client.existIndices(index)){
                    throw DataXException.asDataXException(ElasticWriterErrorCode.INDEX_NOT_EXIST,
                            "cannot get columns from index:[" + index +"]");
                }
                //get properties from index existed
                properties = client.getProps(index, type);
                resolveColumn(outputColumn, null, properties, columnNameSeparator);
                //Reverse outputColumn
                Collections.reverse(outputColumn);
            }
            return properties;
        }

        private void resolveColumn(List<ElasticColumn> outputColumn, ElasticColumn column,
                                   Map<Object, Object> propsMap, String columnNameSeparator){
            propsMap.forEach((key, value) ->{
                if(value instanceof Map){
                    Map metaMap = (Map)value;
                    if(null != metaMap.get(ElasticKey.PROPS_COLUMN_TYPE)){
                        ElasticColumn levelColumn = new ElasticColumn();
                        if(null != column) {
                            levelColumn.setName(column.getName() + columnNameSeparator + key);
                        }else{
                            levelColumn.setName(String.valueOf(key));
                        }
                        levelColumn.setType(String.valueOf(metaMap.get(ElasticKey.PROPS_COLUMN_TYPE)));
                        if(null != metaMap.get(ElasticKey.PROPS_COLUMN_TIMEZONE)){
                            levelColumn.setTimezone(String.valueOf(metaMap.get(ElasticKey.PROPS_COLUMN_TIMEZONE)));
                        }
                        if(null != metaMap.get(ElasticKey.PROPS_COLUMN_FORMAT)){
                            levelColumn.setFormat(String.valueOf(metaMap.get(ElasticKey.PROPS_COLUMN_FORMAT)));
                        }
                        outputColumn.add(levelColumn);
                    }else if(null != metaMap.get(ElasticRestClient.FIELD_PROPS)
                            && metaMap.get(ElasticRestClient.FIELD_PROPS) instanceof Map){
                        ElasticColumn levelColumn = column;
                        if(null == levelColumn){
                            levelColumn = new ElasticColumn();
                            levelColumn.setName(String.valueOf(key));
                        }else{
                            levelColumn.setName(levelColumn.getName() + columnNameSeparator + key);
                        }
                        resolveColumn(outputColumn, levelColumn, (Map)metaMap.get(ElasticRestClient.FIELD_PROPS),
                                columnNameSeparator);
                    }
                }
            });
        }
    }


    public static class Task extends Writer.Task{
        private static final Logger logger = LoggerFactory.getLogger(Task.class);
        private volatile boolean bulkError;
        private Configuration taskConf;
        private String indexName;
        private String typeName;
        private String columnNameSeparator = ElasticColumn.DEFAULT_NAME_SPLIT;
        private List<ElasticColumn> columns;
        private ElasticRestClient restClient;
        private BulkProcessor bulkProcessor;

        @Override
        public void init() {
            this.taskConf = super.getPluginJobConf();
            indexName = this.taskConf.getString(ElasticKey.INDEX_NAME);
            typeName = this.taskConf.getString(ElasticKey.INDEX_TYPE, ElasticRestClient.MAPPING_TYPE_DEFAULT);
            columnNameSeparator = this.taskConf.getString(ElasticKey.COLUMN_NAME_SEPARATOR, ElasticColumn.DEFAULT_NAME_SPLIT);
            int batchSize = this.taskConf.getInt(ElasticKey.BULK_ACTIONS, 1000);
            int bulkPerTask = this.taskConf.getInt(ElasticKey.BULK_PER_TASK, 1);
            columns = JSON.parseObject(this.taskConf.getString(ElasticKey.PROPS_COLUMN), new TypeReference<List<ElasticColumn>>(){
            });
            String userName = this.taskConf.getString(ElasticKey.USERNAME, "");
            String password = this.taskConf.getString(ElasticKey.PASSWORD, "");
            if(StringUtils.isNotBlank(password)){
                try {
                    password = (String) CryptoUtils.string2Object(password);
                } catch (Exception e) {
                    throw DataXException.asDataXException(ElasticWriterErrorCode.CONFIG_ERROR, "decrypt password failed");
                }
            }
            String[] endPoints = this.taskConf.getString(ElasticKey.ENDPOINTS).split(DEFAULT_ENDPOINT_SPLIT);
            if(StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)){
                restClient = ElasticRestClient.custom(endPoints, userName,
                        password, this.taskConf.getMap(ElasticKey.CLIENT_CONFIG));
            }else{
                restClient = ElasticRestClient.custom(endPoints, this.taskConf.getMap(ElasticKey.CLIENT_CONFIG));
            }
            this.bulkProcessor = restClient.createBulk(buildListener(getTaskPluginCollector()), batchSize, bulkPerTask);
        }

        @Override
        public void startWrite(BasicDataReceiver<Object> receiver, Class<?> type) {
            if(type.equals(DocWriteRequest.class)){
                logger.info("Begin to write record to ElasticSearch");
                long count = 0;
                DocWriteRequest request = null;
                while(null != (request = (DocWriteRequest) receiver.getFromReader())){
                    request.index(indexName);
                    request.type(typeName);
                    if(bulkError){
                        throw DataXException.asDataXException(ElasticWriterErrorCode.BULK_REQ_ERROR, "");
                    }
                    this.bulkProcessor.add(request);
                    count += 1;
                }
                this.bulkProcessor.close();
                getTaskPluginCollector().collectMessage(WRITE_SIZE, String.valueOf(count));
                logger.info("End to write record to ElasticSearch");
            }else{
                super.startWrite(receiver, type);
            }
        }

        @Override
        public void startWrite(RecordReceiver lineReceiver) {
            logger.info("Begin to write record to ElasticSearch");
            Record record = null;
            long count = 0;
            while(null != (record = lineReceiver.getFromReader())){
                Map<String, Object> data = ElasticColumn.toData(record, columns, columnNameSeparator);
                IndexRequest request = new IndexRequest(indexName, typeName);
                request.source(data);
                if(bulkError){
                    throw DataXException.asDataXException(ElasticWriterErrorCode.BULK_REQ_ERROR, "");
                }
                this.bulkProcessor.add(request);
                count += 1;
            }
            this.bulkProcessor.close();
            getTaskPluginCollector().collectMessage(WRITE_SIZE, String.valueOf(count));
            logger.info("End to write record to ElasticSearch");
        }

        @Override
        public void destroy() {
            if(null != restClient){
                restClient.close();
            }
        }


        private BulkProcessor.Listener buildListener(final TaskPluginCollector pluginCollector){
            return new BulkProcessor.Listener() {
                @Override
                public void beforeBulk(long l, BulkRequest bulkRequest) {
                    bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.NONE);
                    logger.trace("do_bulk: " + bulkRequest.getDescription());
                }

                @Override
                public void afterBulk(long l, BulkRequest bulkRequest, BulkResponse bulkResponse) {
                    BulkItemResponse[] response = bulkResponse.getItems();
                    for (BulkItemResponse itemResponse : response) {
                        if (itemResponse.isFailed()) {
                            List<String> message = new ArrayList<>();
                            message.add(String.valueOf(itemResponse.getFailure().getStatus().getStatus()));
                            message.add(itemResponse.getId());
                            message.add(itemResponse.getFailureMessage());
                            pluginCollector.collectDirtyRecord(new DirtyRecord(), null, JSON.toJSONString(message));
                        }
                    }
                }

                @Override
                public void afterBulk(long l, BulkRequest bulkRequest, Throwable throwable) {
                    //Ignore interrupted error
                    if(!(throwable instanceof  InterruptedException)){
                        logger.error(throwable.getMessage(), throwable);
                    }
                   bulkError = true;
                }
            };
        }
    }
}
