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
 
package org.apache.linkis.cs.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.linkis.cs.condition.Condition;
import org.apache.linkis.cs.condition.ConditionType;
import org.apache.linkis.cs.condition.construction.ConditionParser;
import org.apache.linkis.server.BDPJettyServerHelper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class ApiJsonTest {

    @Test
    public void test() throws IOException {
        String apiString = "{\n" +
                "\t\"type\":\"And\",\n" +
                "\t\"left\":{\n" +
                "\t\t\"type\":\"ContextType\",\n" +
                "\t\t\"contextType\":\"DATA\"\n" +
                "\t},\n" +
                "\t\"right\":{\n" +
                "\t\t\"type\":\"And\",\n" +
                "\t\t\"left\":{\n" +
                "\t\t\t\"type\":\"ContextScope\",\n" +
                "\t\t\t\"contextScope\":\"PRIVATE\"\n" +
                "\t\t},\n" +
                "\t\t\"right\":{\n" +
                "\t\t\t\"type\":\"Regex\",\n" +
                "\t\t\t\"regex\":\"[abc]]\"\n" +
                "\t\t}\n" +
                "\t}\n" +
                "}";

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(apiString);
        Map<Object, Object> conditionMap = objectMapper.convertValue(jsonNode, new TypeReference<Map<Object, Object>>(){});
        ConditionParser conditionParser = ConditionParser.parserMap.get(conditionMap.get("type"));
        Condition condition = conditionParser.parse(conditionMap);
        Assert.assertEquals(condition.getConditionType(), ConditionType.Logic);

    }

    @Test
    public void temp() {
        String test = "{\"cols\":[{\"name\":\"birthday\",\"visualType\":\"string\",\"type\":\"category\",\"config\":true,\"field\":{\"alias\":\"\",\"desc\":\"\",\"useExpression\":false},\"format\":{\"formatType\":\"default\"},\"from\":\"cols\"},{\"name\":\"name\",\"visualType\":\"string\",\"type\":\"category\",\"config\":true,\"field\":{\"alias\":\"\",\"desc\":\"\",\"useExpression\":false},\"format\":{\"formatType\":\"default\"},\"from\":\"cols\"}],\"rows\":[],\"metrics\":[{\"name\":\"score@Visualis@6F01974E\",\"visualType\":\"number\",\"type\":\"value\",\"agg\":\"sum\",\"config\":true,\"chart\":{\"id\":1,\"name\":\"table\",\"title\":\"表格\",\"icon\":\"icon-table\",\"coordinate\":\"other\",\"rules\":[{\"dimension\":[0,9999],\"metric\":[0,9999]}],\"data\":{\"cols\":{\"title\":\"列\",\"type\":\"category\"},\"rows\":{\"title\":\"行\",\"type\":\"category\"},\"metrics\":{\"title\":\"指标\",\"type\":\"value\"},\"filters\":{\"title\":\"筛选\",\"type\":\"all\"}},\"style\":{\"table\":{\"fontFamily\":\"PingFang SC\",\"fontSize\":\"12\",\"color\":\"#666\",\"lineStyle\":\"solid\",\"lineColor\":\"#D9D9D9\",\"headerBackgroundColor\":\"#f7f7f7\",\"headerConfig\":[],\"columnsConfig\":[],\"leftFixedColumns\":[],\"rightFixedColumns\":[],\"headerFixed\":true,\"autoMergeCell\":false,\"bordered\":true,\"size\":\"default\",\"withPaging\":true,\"pageSize\":\"20\",\"withNoAggregators\":false},\"spec\":{}}},\"field\":{\"alias\":\"\",\"desc\":\"\",\"useExpression\":false},\"format\":{\"formatType\":\"default\"},\"from\":\"metrics\"}],\"filters\":[],\"color\":{\"title\":\"颜色\",\"type\":\"category\",\"value\":{\"all\":\"#509af2\"},\"items\":[]},\"chartStyles\":{\"richText\":{\"content\":\"<p>〖@dv_name_dv@〗</p><p><strong style=\\\"color: rgb(230, 0, 0);\\\">〖@dv_birthday_dv@〗</strong></p>\"},\"spec\":{}},\"selectedChart\":15,\"data\":[],\"pagination\":{\"pageNo\":0,\"pageSize\":0,\"withPaging\":false,\"totalCount\":0},\"dimetionAxis\":\"col\",\"renderType\":\"rerender\",\"orders\":[],\"mode\":\"chart\",\"model\":{\"birthday\":{\"sqlType\":\"STRING\",\"visualType\":\"string\",\"modelType\":\"category\"},\"score\":{\"sqlType\":\"DOUBLE\",\"visualType\":\"number\",\"modelType\":\"value\"},\"teacher\":{\"sqlType\":\"STRING\",\"visualType\":\"string\",\"modelType\":\"category\"},\"city\":{\"sqlType\":\"STRING\",\"visualType\":\"string\",\"modelType\":\"category\"},\"sex\":{\"sqlType\":\"STRING\",\"visualType\":\"string\",\"modelType\":\"category\"},\"fee\":{\"sqlType\":\"DOUBLE\",\"visualType\":\"number\",\"modelType\":\"value\"},\"name\":{\"sqlType\":\"STRING\",\"visualType\":\"string\",\"modelType\":\"category\"},\"lesson\":{\"sqlType\":\"STRING\",\"visualType\":\"string\",\"modelType\":\"category\"},\"id\":{\"sqlType\":\"INT\",\"visualType\":\"number\",\"modelType\":\"value\"},\"class\":{\"sqlType\":\"STRING\",\"visualType\":\"string\",\"modelType\":\"category\"},\"exam_date\":{\"sqlType\":\"STRING\",\"visualType\":\"string\",\"modelType\":\"category\"},\"age\":{\"sqlType\":\"INT\",\"visualType\":\"number\",\"modelType\":\"value\"}},\"controls\":[],\"computed\":[],\"cache\":false,\"expired\":300,\"autoLoadData\":true,\"query\":{\"groups\":[\"birthday\",\"name\"],\"aggregators\":[{\"column\":\"score\",\"func\":\"sum\"}],\"filters\":[],\"orders\":[],\"pageNo\":0,\"pageSize\":0,\"nativeQuery\":false,\"cache\":false,\"expired\":0,\"flush\":false}}";
        Set<String> columns = getWidgetUsedColumns(test);
        columns.size();
    }

    private Set<String> getWidgetUsedColumns(String config){
        Set<String> columns = Sets.newHashSet();
        JsonObject configJson = BDPJettyServerHelper.gson().fromJson(config, JsonElement.class).getAsJsonObject();
        configJson.getAsJsonArray("rows").forEach(e -> columns.add(getRealColumn(e.getAsJsonObject().get("name").getAsString())));
        configJson.getAsJsonArray("cols").forEach(e -> columns.add(getRealColumn(e.getAsJsonObject().get("name").getAsString())));
        configJson.getAsJsonArray("metrics").forEach(e -> columns.add(getRealColumn(e.getAsJsonObject().get("name").getAsString())));
        return columns;
    }

    private String getRealColumn(String wrappedColumn){
        return wrappedColumn.split("@")[0];
    }
}
