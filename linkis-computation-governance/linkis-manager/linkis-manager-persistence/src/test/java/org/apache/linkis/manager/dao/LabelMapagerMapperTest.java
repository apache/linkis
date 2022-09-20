package org.apache.linkis.manager.dao;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.common.entity.label.LabelKeyValue;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceLabelRel;
import org.apache.linkis.manager.common.entity.persistence.PersistenceNode;
import org.apache.linkis.manager.common.entity.persistence.PersistenceResource;
import org.apache.linkis.manager.entity.Tunple;
import org.apache.linkis.manager.label.entity.Label;
import org.h2.tools.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LabelMapagerMapperTest extends BaseDaoTest {

    @Autowired
    LabelManagerMapper labelManagerMapper;

    @Autowired
    ResourceManagerMapper resourceManagerMapper;


    @BeforeAll
    @DisplayName("Each unit test method is executed once before execution")
    protected static void beforeAll() throws Exception {
        Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
    }

    @AfterAll
    @DisplayName("Each unit test method is executed once before execution")
    protected static void afterAll() throws Exception {
    }

    private String labelKey = "testLabelKey";
    private String labelValue = "testLabelValue";
    private String user = "King";
    private String instance = "testInstance";
    private Integer labelId = 9999;
    private Integer resourceId = 8888;

    private PersistenceLabel genPersistenceLabel(){
        Date now = new Date(System.currentTimeMillis());
        PersistenceLabel pl = new PersistenceLabel();
        pl.setLabelKey(labelKey);
        pl.setStringValue(labelValue);
        pl.setValue(new HashMap<>());
        pl.setCreator(user);
        pl.setCreateTime(now);
        pl.setUpdateTime(now);
        return pl;
    }

    @Test
    public void testRegisterLabel(){
        PersistenceLabel pl = insertLabel();
        assertNotNull(pl.getId());
        System.out.println(pl.getId());
    }

    @Test
    void testGetLabel() {
        PersistenceLabel pl = insertLabel();
        PersistenceLabel label = labelManagerMapper.getLabel(pl.getId());
        assertEquals(labelKey, label.getLabelKey());
    }

    @Test
    void testRegisterLabelKeyValues() {
        Map<String, String> labelValueKeyAndContent = new HashMap<>();
        //(#{valueKey}, #{valueContent},#{labelId},now(),now())
        labelValueKeyAndContent.put("valueKey", labelKey);
        labelValueKeyAndContent.put("valueContent", labelValue);
        labelManagerMapper.registerLabelKeyValues(labelValueKeyAndContent, labelId);

    }

    @Test
    void testReplaceIntoLabelKeyValue() {
        labelManagerMapper.replaceIntoLabelKeyValue(labelKey, labelValue, labelId);
    }

    @Test
    void testDeleteLabel() {
        PersistenceLabel pl = insertLabel();
        PersistenceLabel label = labelManagerMapper.getLabel(pl.getId());
        assertEquals(labelKey, label.getLabelKey());
        labelManagerMapper.deleteLabel(pl.getId());
        PersistenceLabel newLabel = labelManagerMapper.getLabel(pl.getId());
        assertNull(newLabel);
    }

    @Test
    void deleteByLabel() {
        PersistenceLabel pl = insertLabel();
        PersistenceLabel label = labelManagerMapper.getLabel(pl.getId());
        assertEquals(labelKey, label.getLabelKey());
        labelManagerMapper.deleteByLabel(labelKey, labelValue);
        PersistenceLabel newLabel = labelManagerMapper.getLabel(pl.getId());
        assertNull(newLabel);
    }

    @Test
    void testDeleteLabelKeyValues() {
        labelManagerMapper.deleteLabelKeyVaules(labelId);
    }

    @Test
    void testUpdateLabel() {
        PersistenceLabel pl = insertLabel();
        pl.setLabelKey("testUpdate");
        labelManagerMapper.updateLabel(pl.getId(), pl);
        PersistenceLabel label = labelManagerMapper.getLabel(pl.getId());
        assertEquals("testUpdate", label.getLabelKey());
    }

    @Test
    void testAddLabelServiceInstance() {
        labelManagerMapper.addLabelServiceInstance(instance, Arrays.asList(labelId));
        List<Integer> ids = labelManagerMapper.getLabelIdsByInstance(instance);
        assertEquals(ids.get(0), labelId);
    }

    @Test
    void testGetLabelByServiceInstance() {
        PersistenceLabel pl = insertLabel();
        labelManagerMapper.addLabelServiceInstance(instance, Arrays.asList(pl.getId()));
        List<PersistenceLabel> serviceInstance = labelManagerMapper.getLabelByServiceInstance(instance);
        assertEquals(serviceInstance.get(0).getLabelKey(), labelKey);
    }

    @Test
    void testGetLabelByResource() {
        PersistenceLabel pl = insertLabel();
        labelManagerMapper.addLabelsAndResource(resourceId, Arrays.asList(pl.getId()));
        PersistenceResource pr = new PersistenceResource();
        pr.setId(resourceId);
        List<PersistenceLabel> labels = labelManagerMapper.getLabelByResource(pr);
        assertEquals(labels.size(), 1);
    }

    @Test
    void testGetResourcesByLabel() {
        List<PersistenceResource> resources = labelManagerMapper.getResourcesByLabel(labelKey, labelValue);
        assertEquals(resources.size(), 0);
    }

    @Test
    void testGetLabelIdsByInstance() {
        labelManagerMapper.addLabelServiceInstance(instance, Arrays.asList(1, 2));
        List<Integer> ids = labelManagerMapper.getLabelIdsByInstance(instance);
        assertEquals(ids.size(), 2);
    }

    @Test
    void testGetLabelsByInstance() {
        PersistenceLabel pl = insertLabel();
        labelManagerMapper.addLabelServiceInstance(instance, Arrays.asList(pl.getId()));
        List<PersistenceLabel> labels = labelManagerMapper.getLabelsByInstance(instance);
        assertEquals(labels.get(0).getLabelKey(), labelKey);
    }

    @Test
    void testGetInstanceByLabelId() {
        List<PersistenceNode> labels = labelManagerMapper.getInstanceByLabelId(labelId);
        assertEquals(labels.size(), 0);
    }

    @Test
    void testGetResourcesByLabels() {
        PersistenceLabel pl = genPersistenceLabel();
        List<PersistenceResource> resourcesByLabels = labelManagerMapper.getResourcesByLabels(Arrays.asList(pl));
        assertEquals(resourcesByLabels.size(), 0);
    }

    @Test
    void testGetInstanceIdsByLabelIds() {
        labelManagerMapper.addLabelServiceInstance(instance, Arrays.asList(labelId));
        List<String> list = labelManagerMapper.getInstanceIdsByLabelIds(Arrays.asList(labelId));
        assertEquals(list.get(0), instance);
    }

    @Test
    void testGetLabelsByLabelIds() {
        PersistenceLabel pl = insertLabel();
        List<PersistenceLabel> labels = labelManagerMapper.getLabelsByLabelIds(Arrays.asList(pl.getId(), 1, 2));
        assertEquals(labels.size(), 1);

    }

    private PersistenceLabel insertLabel() {
        PersistenceLabel pl = genPersistenceLabel();
        labelManagerMapper.registerLabel(pl);
        return pl;
    }

    @Test
    void testAddLabelsByUser() {
        labelManagerMapper.addLabelsByUser(user, Arrays.asList(labelId));
        List<String> list = labelManagerMapper.getUserNameByLabelId(labelId);
        assertEquals(list.get(0), user);
    }

    @Test
    void testGetUserNamesByLabelIds() {
        labelManagerMapper.addLabelsByUser(user, Arrays.asList(labelId));
        List<String> list = labelManagerMapper.getUserNamesByLabelIds(Arrays.asList(labelId, 1, 2));
        assertEquals(list.get(0), user);
    }

    @Test
    void testGetLabelsByUser() {
        PersistenceLabel pl = insertLabel();
        labelManagerMapper.addLabelsByUser(user, Arrays.asList(pl.getId()));
        List<PersistenceLabel> list = labelManagerMapper.getLabelsByUser(user);
        assertEquals(list.get(0).getLabelKey(), labelKey);
    }

    @Test
    void testGetLabelsByLabelKey() {
        PersistenceLabel pl = insertLabel();
        List<PersistenceLabel> labels = labelManagerMapper.getLabelsByLabelKey(labelKey);
        assertEquals(labels.get(0).getLabelKey(), labelKey);
    }

    @Test
    void testDeleteLabelIdsAndInstance(){
        PersistenceLabel pl = insertLabel();
        labelManagerMapper.addLabelServiceInstance(instance, Arrays.asList(pl.getId()));
        List<PersistenceLabel> labels = labelManagerMapper.getLabelsByInstance(instance);
        assertEquals(labels.get(0).getLabelKey(), labelKey);
        labelManagerMapper.deleteLabelIdsAndInstance(instance, Arrays.asList(pl.getId()));
        List<PersistenceLabel> labels1 = labelManagerMapper.getLabelsByInstance(instance);
        assertEquals(labels1.size(), 0);
    }

    @Test
    void testDeleteLabelIdsByUser(){
        PersistenceLabel pl = insertLabel();
        labelManagerMapper.addLabelsByUser(user, Arrays.asList(pl.getId()));
        List<PersistenceLabel> list = labelManagerMapper.getLabelsByUser(user);
        assertEquals(list.get(0).getLabelKey(), labelKey);
        labelManagerMapper.deleteLabelIdsByUser(user, Arrays.asList(pl.getId()));
        List<PersistenceLabel> list1 = labelManagerMapper.getLabelsByUser(user);
        assertEquals(list1.size(), 0);

    }

    @Test
    void testDeleteUserById(){
        labelManagerMapper.addLabelsByUser(user, Arrays.asList(labelId));
        List<String> list = labelManagerMapper.getUserNameByLabelId(labelId);
        assertEquals(list.get(0), user);
        labelManagerMapper.deleteUserById(labelId);
        List<String> list1 = labelManagerMapper.getUserNameByLabelId(labelId);
        assertEquals(list1.size(), 0);
    }

    @Test
    void testDimListNodeRelationsByKeyValueMap(){
        Map<String, String> map = new HashMap<>();
        map.put("valueKey", labelKey);
        map.put("valueContent", labelValue);
        ArrayList<Map<String, String>> list = new ArrayList<>();
        list.add(map);
        List<PersistenceLabel> labels = labelManagerMapper.dimListLabelByValueList(list, "test");
        assertEquals(labels.size(), 0);
    }

    @Test
    void testGetNodeRelationsByLabels(){
        PersistenceLabel pl = insertLabel();
        List<PersistenceLabel> list = new ArrayList<>();
        List<Map<String, Object>> labels = labelManagerMapper.getNodeRelationsByLabels(list);
        assertEquals(labels.size(), 0);
    }

    @Test
    void testGetLabelByKeyValue(){
        insertLabel();
        PersistenceLabel labelByKeyValue = labelManagerMapper.getLabelByKeyValue(labelKey, labelValue);
        assertEquals(labelByKeyValue.getLabelKey(), labelKey);
    }

    @Test
    void testDeleteResourceByLabelId(){
        PersistenceLabel pl = insertLabel();
        labelManagerMapper.addLabelsAndResource(resourceId, Arrays.asList(pl.getId()));
        PersistenceResource pr = new PersistenceResource();
        pr.setId(resourceId);
        List<PersistenceLabel> labels = labelManagerMapper.getLabelByResource(pr);
        assertEquals(labels.size(), 1);
        labelManagerMapper.deleteResourceByLabelId(pl.getId());
        List<PersistenceLabel> labels1 = labelManagerMapper.getLabelByResource(pr);
        assertEquals(labels1.size(), 0);
    }

    @Test
    void testDeleteResourceByLabelIdIndirect(){
        PersistenceLabel pl = insertLabel();
        labelManagerMapper.addLabelsAndResource(resourceId, Arrays.asList(pl.getId()));
        PersistenceResource pr = new PersistenceResource();
        pr.setId(resourceId);
        List<PersistenceLabel> labels = labelManagerMapper.getLabelByResource(pr);
        assertEquals(labels.size(), 1);

        resourceManagerMapper.registerResource(pr);
        PersistenceResource resource = resourceManagerMapper.getResourceById(pr.getId());
        assertNotNull(resource);
        labelManagerMapper.deleteResourceByLabelIdInDirect(pl.getId());
        PersistenceResource resource1 = resourceManagerMapper.getResourceById(pr.getId());
        assertNull(resource1);
    }

    @Test
    void testSelectLabelIdByLabelKeyValuesMaps(){
        //
        PersistenceLabel pl = insertLabel();
        labelManagerMapper.addLabelsAndResource(resourceId, Arrays.asList(pl.getId()));
        labelManagerMapper.replaceIntoLabelKeyValue(labelKey, labelValue, pl.getId());
        Map<String, Map<String, String>> singletonMap = new HashMap<>();
        Map<String, String> map = new HashMap<>();
        map.put(labelKey, labelValue);
        singletonMap.put(labelKey, map);
        Integer id = labelManagerMapper.selectLabelIdByLabelKeyValuesMaps(singletonMap);
        assertNull(id);
    }

    @Test
    void testDeleteResourceByLabelKeyValuesMaps(){
        PersistenceLabel pl = insertLabel();
        labelManagerMapper.addLabelsAndResource(resourceId, Arrays.asList(pl.getId()));
        PersistenceResource pr = new PersistenceResource();
        pr.setId(resourceId);
        List<PersistenceLabel> labels = labelManagerMapper.getLabelByResource(pr);
        assertEquals(labels.size(), 1);
        labelManagerMapper.deleteResourceByLabelKeyValuesMaps(pl.getId());
        List<PersistenceLabel> labels1 = labelManagerMapper.getLabelByResource(pr);
        assertEquals(labels1.size(), 0);
    }

    @Test
    void testDeleteResourceByLabelKeyValuesMapsInDirect(){
        PersistenceLabel pl = insertLabel();
        labelManagerMapper.addLabelsAndResource(resourceId, Arrays.asList(pl.getId()));
        PersistenceResource pr = new PersistenceResource();
        pr.setId(resourceId);
        List<PersistenceLabel> labels = labelManagerMapper.getLabelByResource(pr);
        assertEquals(labels.size(), 1);

        resourceManagerMapper.registerResource(pr);
        PersistenceResource resource = resourceManagerMapper.getResourceById(pr.getId());
        assertNotNull(resource);
        labelManagerMapper.deleteResourceByLabelKeyValuesMapsInDirect(pl.getId());
        PersistenceResource resource1 = resourceManagerMapper.getResourceById(pr.getId());
        assertNull(resource1);
    }

    @Test
    void testBatchDeleteResourceByLabelId(){
        PersistenceLabel pl = insertLabel();
        labelManagerMapper.addLabelsAndResource(resourceId, Arrays.asList(pl.getId()));
        PersistenceResource pr = new PersistenceResource();
        pr.setId(resourceId);
        List<PersistenceLabel> labels = labelManagerMapper.getLabelByResource(pr);
        assertEquals(1, labels.size() );

        resourceManagerMapper.registerResource(pr);
        PersistenceResource resource = resourceManagerMapper.getResourceById(pr.getId());
        assertNotNull(resource);

        labelManagerMapper.batchDeleteResourceByLabelId(Arrays.asList(pr.getId()));

        PersistenceResource resource1 = resourceManagerMapper.getResourceById(pr.getId());
        assertNotNull(resource1);

    }

    @Test
    void testListLabelBySqlPattern(){
        PersistenceLabel pl = insertLabel();
        labelManagerMapper.addLabelsAndResource(resourceId, Arrays.asList(pl.getId()));
        List<PersistenceLabelRel> list = labelManagerMapper.listLabelBySQLPattern("%test%", labelKey);
        assertEquals(1, list.size() );
    }
}



