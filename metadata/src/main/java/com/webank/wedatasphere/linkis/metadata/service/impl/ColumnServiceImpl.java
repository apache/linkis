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

package com.webank.wedatasphere.linkis.metadata.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.webank.wedatasphere.linkis.metadata.dao.ColumnDao;
import com.webank.wedatasphere.linkis.metadata.domain.Column;
import com.webank.wedatasphere.linkis.metadata.domain.DataSource;
import com.webank.wedatasphere.linkis.metadata.domain.View;
import com.webank.wedatasphere.linkis.metadata.service.ColumnService;
import com.webank.wedatasphere.linkis.metadata.util.Constants;
import com.webank.wedatasphere.linkis.metadata.util.DWSConfig;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
/**
 * Created by shanhuang on 9/13/18.
 */
@Service
public class ColumnServiceImpl implements ColumnService {

    @Autowired(required = false)
    ColumnDao columnDao;

    HttpClient client = new HttpClient();

    ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void create(Collection<Column> columns, String userName) throws Exception {
        Set<String> names = Sets.newHashSet();
        columns.forEach(column -> {
            if(names.contains(column.getName())){
                column.generateDuplicationMessage("Field Chinese(字段中文)");
            }
            names.add(column.getName());
            column.setInfoOnCreate(userName);
            columnDao.insert(column);
        });
    }

    @Override
    public void update(Collection<Column> columns, String userName) throws Exception {
        columns.forEach(column -> {
            column.setInfoOnUpdate(userName);
            columnDao.update(column);
        });
    }

    @Override
    public void delete(Integer id) throws Exception {
        columnDao.deleteById(id);
    }

    @Override
    public List<Column> lookup(View view) throws Exception {
        List<Column> columns = Lists.newArrayList();
        DataSource dataSource = view.getDataSource();
        while(dataSource != null){
            if(Constants.TABLE.equals(dataSource.getType())){
                columns.addAll(getColumnsInTable(dataSource));
            }
            dataSource = dataSource.getRight();
        }
        return columns;
    }

    private List<Column> getColumnsInTable(DataSource dataSource)  throws Exception{
        List<Column> columns = Lists.newArrayList();
        String[] dbInfo = StringUtils.split(dataSource.getContent(), '.');
        PostMethod post = new PostMethod(DWSConfig.IDE_URL.getValue() + "api/schema/tables");
        post.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        post.setParameter("dbName", dbInfo[0]);
        post.setParameter("tableName", dbInfo[1]);
        try {
            client.executeMethod(post);
            JsonNode response = jsonMapper.readTree(post.getResponseBodyAsStream());
            Iterator<JsonNode> columnNodes = response.getElements();
            while(columnNodes.hasNext()) {
                JsonNode columnNode = columnNodes.next();
                if(columnNode.has("type")) {
                    Column column = new Column();
                    column.setOriginalName(dataSource.getName() + "." + columnNode.get("text").asText());
                    column.setColumnType(columnNode.get("type").asText());
                    columns.add(column);
                }
            }
        } catch (Exception e) {
            throw new Exception("Failed to get data table content(获取数据表内容失败)。", e);
        } finally {
            post.releaseConnection();
        }

        return columns;
    }


}
