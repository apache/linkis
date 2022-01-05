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
 
import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.common.entity.label.LabelKeyValue;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.dao.LabelManagerMapper;
import org.apache.linkis.manager.entity.Tunple;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.persistence.ResourceLabelPersistence;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;


public class PersistenceTest {

    AnnotationConfigApplicationContext context = null;

    LabelManagerMapper labelManagerMapper = null;

    ResourceLabelPersistence resourceLabelPersistence = null;

    @Before
    public void before() {
        context = new AnnotationConfigApplicationContext(Scan.class);
        labelManagerMapper = context.getBean(LabelManagerMapper.class);
        resourceLabelPersistence = context.getBean(ResourceLabelPersistence.class);
    }

    @Test
    public void test01() throws InvocationTargetException, IllegalAccessException {

        PersistenceLabel persistenceLabel1 = new PersistenceLabel();
        persistenceLabel1.setLabelKey("engineType");
        persistenceLabel1.setValue(Collections.singletonMap("runType", "spark"));
        System.out.println(persistenceLabel1.getStringValue());
        PersistenceLabel persistenceLabel2 = new PersistenceLabel();
        persistenceLabel2.setLabelKey("engineType");
        persistenceLabel2.setValue(Collections.singletonMap("runType", "hive"));
        System.out.println(persistenceLabel2.getStringValue());
        List<PersistenceLabel> persistenceLabels = Arrays.asList(persistenceLabel1);
        List<Map<String, Object>> nodeRelationsByLabels = labelManagerMapper.dimListNodeRelationsByKeyValueMap(Collections.singletonMap(persistenceLabel1.getLabelKey(),persistenceLabel1.getValue()), Label.ValueRelation.ALL.name());
        List<Tunple<PersistenceLabel, ServiceInstance>> arrays = new ArrayList<Tunple<PersistenceLabel, ServiceInstance>>();
        for (Map<String, Object> nodeRelationsByLabel : nodeRelationsByLabels) {
            ServiceInstance serviceInstance = new ServiceInstance();
            PersistenceLabel persistenceLabel = new PersistenceLabel();
            BeanUtils.populate(serviceInstance, nodeRelationsByLabel);
            BeanUtils.populate(persistenceLabel, nodeRelationsByLabel);
            arrays.add(new Tunple(persistenceLabel, serviceInstance));
        }
        Map<PersistenceLabel, List<ServiceInstance>> collect = arrays.stream()
                .collect(Collectors.groupingBy(Tunple::getKey)).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, f -> f.getValue().stream().map(Tunple::getValue).collect(Collectors.toList())));
        System.out.println(collect);
    }


    @Test
    public void test02() throws InvocationTargetException, IllegalAccessException {

        List<ServiceInstance> list = Arrays.asList(ServiceInstance.apply("aaa", "localhost:8088"), ServiceInstance.apply("bbb", "localhost:8089"));
        List<Map<String, Object>> nodeRelationsByLabels = labelManagerMapper.listLabelRelationByServiceInstance(list);
        List<Tunple<ServiceInstance, PersistenceLabel>> arrays = new ArrayList<>();
        for (Map<String, Object> nodeRelationsByLabel : nodeRelationsByLabels) {
            ServiceInstance serviceInstance = new ServiceInstance();
            PersistenceLabel persistenceLabel = new PersistenceLabel();
            BeanUtils.populate(serviceInstance, nodeRelationsByLabel);
            BeanUtils.populate(persistenceLabel, nodeRelationsByLabel);
            arrays.add(new Tunple(serviceInstance, persistenceLabel));
        }
        Map<ServiceInstance, List<PersistenceLabel>> collect = arrays.stream()
                .collect(Collectors.groupingBy(Tunple::getKey)).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, f -> f.getValue().stream().map(Tunple::getValue).collect(Collectors.toList())));
        System.out.println(collect);
    }


    @Test
    public void test03() throws InvocationTargetException, IllegalAccessException {

        List<ServiceInstance> nodeByLabelKeyValue = labelManagerMapper.getNodeByLabelKeyValue("serverAlias", "em");
        System.out.println(nodeByLabelKeyValue);
    }


    @Test
    public void testListResourceLabelByValueList() throws InvocationTargetException, IllegalAccessException {
        /*SELECT l.*,lvr.*
                FROM linkis_cg_manager_label l ,linkis_cg_manager_label_resource lr ,linkis_cg_manager_label_value_relation lvr
        WHERE l.id = lr.label_id AND l.id = lvr.label_id
        AND (lvr.label_value_key,lvr.label_value_content) IN (('alias','em'),('key2','value2'),("instance","localhost:9000"),("serviceName","sparkEngine"))
        GROUP BY l.id HAVING COUNT(1) = l.label_value_size;*/
        ArrayList<LabelKeyValue> labelKeyValues = new ArrayList<>();
        labelKeyValues.add(new LabelKeyValue("alias", "em"));
        labelKeyValues.add(new LabelKeyValue("key2", "value2"));
        List<PersistenceLabel> persistenceLabels = labelManagerMapper.listResourceLabelByValues(labelKeyValues);
        System.out.println(persistenceLabels);
    }

    @Test
    public void testListResourceLabelByKeyValuesMaps() throws InvocationTargetException, IllegalAccessException {
        HashMap<String, Map<String, String>> labelKeyValues = new HashMap<>();
        HashMap<String, String> key1 = new HashMap<>();
        key1.put("instance", "localhost:9026");
        key1.put("serviceName", "linkis-engineManager");
        labelKeyValues.put("emInstance", key1);
        HashMap<String, String> key2 = new HashMap<>();
        key2.put("instance", "localhost:9000");
        key2.put("serviceName", "sparkEngine");
        labelKeyValues.put("engineInstance", key2);
        List<PersistenceLabel> persistenceLabels = labelManagerMapper.dimlistResourceLabelByKeyValueMap(labelKeyValues, Label.ValueRelation.ALL.name());
        System.out.println(persistenceLabels);
    }

    @Test
    public void testGetResourceByLaBelId() {
        List<PersistenceResource> persistenceResources = labelManagerMapper.listResourceByLaBelId(1);
        System.out.println(persistenceResources);
    }

    @Test
    public void testGetResourceByKeyValuesMaps() {
        HashMap<String, Map<String, String>> labelKeyValues = new HashMap<>();
        HashMap<String, String> key1 = new HashMap<>();
        key1.put("alias", "em");
        key1.put("key2", "value2");
        labelKeyValues.put("serverAlias", key1);
        HashMap<String, String> key2 = new HashMap<>();
        key2.put("instance", "localhost:9000");
        key2.put("serviceName", "sparkEngine");
        labelKeyValues.put("engineInstance", key2);
        List<PersistenceLabel> persistenceLabels = labelManagerMapper.dimlistResourceLabelByKeyValueMap(labelKeyValues, Label.ValueRelation.ALL.name());
        System.out.println(persistenceLabels);
    }

    @Test
    public void testDeleteResourceByLabelId() {
        labelManagerMapper.deleteResourceByLabelId(1);
    }

    @Test
    public void testDeleteResourceByLabelKeyValuesMaps() {
        HashMap<String, Map<String, String>> labelKeyValues = new HashMap<>();
        HashMap<String, String> key1 = new HashMap<>();
        key1.put("alias", "em");
        key1.put("key2", "value2");
        labelKeyValues.put("serverAlias", key1);
    }

    @Test
    public void testBatchDeleteResourceByLabelId() {
        labelManagerMapper.batchDeleteResourceByLabelId(Arrays.asList(3, 4));
        labelManagerMapper.batchDeleteResourceByLabelIdInDirect(Arrays.asList(3, 4));
    }

    @Test
    public void testBatchDeleteResourceByLabelKeyValuesMaps() {
        HashMap<String, Map<String, String>> labelKeyValues = new HashMap<>();
        HashMap<String, String> key1 = new HashMap<>();
        key1.put("alias", "em");
        key1.put("key2", "value2");
        labelKeyValues.put("serverAlias", key1);
        HashMap<String, String> key2 = new HashMap<>();
        key2.put("instance", "localhost:9000");
        key2.put("serviceName", "sparkEngine");
        labelKeyValues.put("engineInstance", key2);
        labelManagerMapper.batchDeleteResourceByLabelKeyValuesMaps(labelKeyValues);
    }

    @Test
    public void testSetResource01() {
        HashMap<String, Map<String, String>> labelKeyValues = new HashMap<>();
        HashMap<String, String> key1 = new HashMap<>();
        key1.put("alias", "em");
        key1.put("key2", "value2");
        labelKeyValues.put("serverAlias", key1);
        HashMap<String, String> key2 = new HashMap<>();
        key2.put("instance", "localhost:9000");
        key2.put("serviceName", "sparkEngine");
        labelKeyValues.put("engineInstance", key2);
        PersistenceResource persistenceResource = new PersistenceResource();
        persistenceResource.setMaxResource("100");
        PersistenceLabel persistenceLabel = new PersistenceLabel();
        persistenceLabel.setId(3);
        resourceLabelPersistence.setResourceToLabel(persistenceLabel, persistenceResource);
    }

    @Test
    public void testSetResource02() {
        HashMap<String, Map<String, String>> labelKeyValues = new HashMap<>();
        HashMap<String, String> key1 = new HashMap<>();
        key1.put("alias", "em");
        key1.put("key2", "value2");
        labelKeyValues.put("serverAlias", key1);
        HashMap<String, String> key2 = new HashMap<>();
        key2.put("instance", "localhost:9000");
        key2.put("serviceName", "sparkEngine");
        labelKeyValues.put("engineInstance", key2);
        PersistenceResource persistenceResource = new PersistenceResource();
        persistenceResource.setMaxResource("300");
        PersistenceLabel persistenceLabel = new PersistenceLabel();
        persistenceLabel.setValue(key1);
        persistenceLabel.setLabelKey("serverAlias");
        resourceLabelPersistence.setResourceToLabel(persistenceLabel, persistenceResource);
    }

    @Test
    public void testdimListLabelByValueList() throws InvocationTargetException, IllegalAccessException {
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("type", "spark");
        //stringStringHashMap.put("version","2.4.3");
        stringStringHashMap.put("aaa", "bbb");
        List<PersistenceLabel> persistenceLabels = labelManagerMapper.dimListLabelByValueList(Arrays.asList(stringStringHashMap), Label.ValueRelation.OR.name());
        System.out.println(persistenceLabels.size());
    }

    @Test
    public void testdimListLabelsByKeyValueMap() throws InvocationTargetException, IllegalAccessException {
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("type", "spark");
        //stringStringHashMap.put("version","2.4.3");
        stringStringHashMap.put("aaa", "bbb");
        List<PersistenceLabel> persistenceLabels = labelManagerMapper.dimListLabelByKeyValueMap(Collections.singletonMap("combined_xx_xx", stringStringHashMap), Label.ValueRelation.AND.name());
        System.out.println(persistenceLabels.size());
    }


    @Test
    public void testListLabelByKeyValueMap() throws InvocationTargetException, IllegalAccessException {
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("type", "spark");
        stringStringHashMap.put("version", "2.4.3");
        stringStringHashMap.put("aaa", "bbb");
        stringStringHashMap.put("ccc", "ddd");
        stringStringHashMap.put("eee", "fff");
        List<PersistenceLabel> persistenceLabels = labelManagerMapper.listLabelByKeyValueMap(Collections.singletonMap("combined_xx_xx", stringStringHashMap));
        System.out.println(persistenceLabels.size());
    }
}
