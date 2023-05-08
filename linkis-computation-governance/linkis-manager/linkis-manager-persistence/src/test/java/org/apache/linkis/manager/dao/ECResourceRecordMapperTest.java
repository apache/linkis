package org.apache.linkis.manager.dao;

import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * mysql用h2跑会报错，h2部分函数不支持。pg测试用实体库跑不会报错
 */
class ECResourceRecordMapperTest extends BaseDaoTest {

    @Autowired
    ECResourceRecordMapper ecResourceRecordMapper;

    @Test
    void insertECResourceInfoRecord() {
        ECResourceInfoRecord ecResourceInfoRecord = new ECResourceInfoRecord();
        ecResourceInfoRecord.setUsedResource("usedResource");
        ecResourceInfoRecord.setReleasedResource("{\"instance\":1,\"memory\":\"1024.0 MB\",\"cpu\":1}");
        ecResourceInfoRecord.setRequestResource("{\"instance\":1,\"memory\":\"1024.0 MB\",\"cpu\":1}");
        ecResourceInfoRecord.setStatus("successed");
        ecResourceInfoRecord.setCreateUser("test");
        ecResourceInfoRecord.setEcmInstance("hadoop001:9102");
        ecResourceInfoRecord.setLabelValue("ods-LINKISCLI,shell-1");
        ecResourceInfoRecord.setLogDirSuffix("ods/20220824/shell/617cea23-0735-4df8-a0a9-703cac91f5e5/logs");
        ecResourceInfoRecord.setMetrics("metrics");
        ecResourceInfoRecord.setReleaseTime(new Date());
        ecResourceInfoRecord.setReleaseTimes(1);
        ecResourceInfoRecord.setServiceInstance("hadoop001:44835");
        ecResourceInfoRecord.setTicketId("617cea23-0735-4df8-a0a9-703cac91f5e5");
        ecResourceInfoRecord.setUsedTimes(1);
        ecResourceInfoRecord.setUsedTime(new Date());

        ecResourceRecordMapper.insertECResourceInfoRecord(ecResourceInfoRecord);

        List<String> stringList = new ArrayList<>();
        stringList.add("hadoop001:44835");
        stringList.add("hadoop002:44835");
        List<String> stringList2 = new ArrayList<>();
        stringList2.add("1");
        stringList2.add("2");
        List<ECResourceInfoRecord> list = ecResourceRecordMapper.getECResourceInfoList(stringList, stringList2);
        assertTrue(list.size() >= 1);
    }

    @Test
    void getECResourceInfoRecord() {
        insertECResourceInfoRecord();
        ECResourceInfoRecord ecResourceInfoRecord = ecResourceRecordMapper.getECResourceInfoRecord("617cea23-0735-4df8-a0a9-703cac91f5e5");
        assertTrue(ecResourceInfoRecord != null);
    }

    @Test
    void getECResourceInfoRecordByInstance() {
        insertECResourceInfoRecord();
        ECResourceInfoRecord ecResourceInfoRecord = ecResourceRecordMapper.getECResourceInfoRecordByInstance("hadoop001:44835");
        assertTrue(ecResourceInfoRecord != null);
    }

    @Test
    void updateECResourceInfoRecord() {
        insertECResourceInfoRecord();
        ECResourceInfoRecord select = ecResourceRecordMapper.getECResourceInfoRecordByInstance("hadoop001:44835");
        Integer updateId = select.getId();

        ECResourceInfoRecord ecResourceInfoRecord = new ECResourceInfoRecord();
        ecResourceInfoRecord.setId(updateId);
        ecResourceInfoRecord.setUsedResource("usedResource2");
        ecResourceInfoRecord.setReleasedResource("{\"instance\":1,\"memory\":\"1024.0 MB\",\"cpu\":2}");
        ecResourceInfoRecord.setRequestResource("{\"instance\":1,\"memory\":\"1024.0 MB\",\"cpu\":2}");
        ecResourceInfoRecord.setStatus("successed2");
        ecResourceInfoRecord.setCreateUser("test2");
        ecResourceInfoRecord.setEcmInstance("hadoop001:91022");
        ecResourceInfoRecord.setLabelValue("ods-LINKISCLI,shell-12");
        ecResourceInfoRecord.setLogDirSuffix("ods/20220824/shell/617cea23-0735-4df8-a0a9-703cac91f5e5/logs2");
        ecResourceInfoRecord.setMetrics("metrics2");
        ecResourceInfoRecord.setReleaseTimes(12);
        ecResourceInfoRecord.setServiceInstance("hadoop001:448352");
        ecResourceInfoRecord.setUsedTimes(2);
        ecResourceRecordMapper.updateECResourceInfoRecord(ecResourceInfoRecord);
        List<String> stringList = new ArrayList<>();
        stringList.add("hadoop001:448352");
        List<ECResourceInfoRecord> list = ecResourceRecordMapper.getECResourceInfoList(stringList, null);
        assertTrue(list.size() > 0);
    }

    @Test
    void deleteECResourceInfoRecordByTicketId() {
        insertECResourceInfoRecord();
        ecResourceRecordMapper.deleteECResourceInfoRecordByTicketId("617cea23-0735-4df8-a0a9-703cac91f5e5");
        List<String> stringList = new ArrayList<>();
        stringList.add("hadoop001:44835");
        stringList.add("hadoop002:44835");
        List<String> stringList2 = new ArrayList<>();
        stringList2.add("1");
        stringList2.add("2");
        List<ECResourceInfoRecord> list = ecResourceRecordMapper.getECResourceInfoList(stringList, stringList2);
        assertTrue(list.size() == 0);
    }

    @Test
    void deleteECResourceInfoRecord() {
        insertECResourceInfoRecord();
        ECResourceInfoRecord select = ecResourceRecordMapper.getECResourceInfoRecordByInstance("hadoop001:44835");
        Integer delId = select.getId();
        ecResourceRecordMapper.deleteECResourceInfoRecord(delId);
        List<String> stringList = new ArrayList<>();
        stringList.add("hadoop001:44835");
        stringList.add("hadoop002:44835");
        List<String> stringList2 = new ArrayList<>();
        stringList2.add("1");
        stringList2.add("2");
        List<ECResourceInfoRecord> list = ecResourceRecordMapper.getECResourceInfoList(stringList, stringList2);
        assertTrue(list.size() == 0);
    }

    @Test
    void getECResourceInfoHistory() {
        insertECResourceInfoRecord();
        List<ECResourceInfoRecord> list = ecResourceRecordMapper.getECResourceInfoHistory("test", "hadoop001:44835", null, null, "shell-1");
        assertTrue(list.size() >= 1);
    }

    @Test
    void getECResourceInfoList() {
        insertECResourceInfoRecord();
        List<String> stringList = new ArrayList<>();
        stringList.add("hadoop001:44835");
        stringList.add("hadoop002:44835");
        List<String> stringList2 = new ArrayList<>();
        stringList2.add("1");
        stringList2.add("2");
        List<ECResourceInfoRecord> list = ecResourceRecordMapper.getECResourceInfoList(stringList, stringList2);
        assertTrue(list.size() >= 1);
    }

}