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

package org.apache.linkis.manager.dao;

import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ECResourceRecordMapperTest extends BaseDaoTest {

  @Autowired ECResourceRecordMapper ecResourceRecordMapper;

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
    ecResourceInfoRecord.setLogDirSuffix(
        "ods/20220824/shell/617cea23-0735-4df8-a0a9-703cac91f5e5/logs");
    ecResourceInfoRecord.setMetrics("metrics");
    ecResourceInfoRecord.setReleaseTime(new Date());
    ecResourceInfoRecord.setReleaseTimes(1);
    ecResourceInfoRecord.setServiceInstance("hadoop001:44835");
    ecResourceInfoRecord.setTicketId("617cea23-0735-4df8-a0a9-703cac91f5e5");
    ecResourceInfoRecord.setUsedTimes(1);
    ecResourceInfoRecord.setUsedTime(new Date());

    ecResourceRecordMapper.insertECResourceInfoRecord(ecResourceInfoRecord);
    ECResourceInfoRecord queryEcResourceInfoRecord =
        ecResourceRecordMapper.getECResourceInfoRecord("617cea23-0735-4df8-a0a9-703cac91f5e5");
    assertTrue(queryEcResourceInfoRecord != null);
  }

  @Test
  void getECResourceInfoRecord() {
    insertECResourceInfoRecord();
    ECResourceInfoRecord ecResourceInfoRecord =
        ecResourceRecordMapper.getECResourceInfoRecord("617cea23-0735-4df8-a0a9-703cac91f5e5");
    assertTrue(ecResourceInfoRecord != null);
  }

  @Test
  void getECResourceInfoRecordByInstance() {
    insertECResourceInfoRecord();
    ECResourceInfoRecord ecResourceInfoRecord =
        ecResourceRecordMapper.getECResourceInfoRecordByInstance("hadoop001:44835");
    assertTrue(ecResourceInfoRecord != null);
  }

  @Test
  void updateECResourceInfoRecord() {
    insertECResourceInfoRecord();
    ECResourceInfoRecord select =
        ecResourceRecordMapper.getECResourceInfoRecordByInstance("hadoop001:44835");
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
    ecResourceInfoRecord.setLogDirSuffix(
        "ods/20220824/shell/617cea23-0735-4df8-a0a9-703cac91f5e5/logs2");
    ecResourceInfoRecord.setMetrics("metrics2");
    ecResourceInfoRecord.setReleaseTimes(12);
    ecResourceInfoRecord.setServiceInstance("hadoop001:448352");
    ecResourceInfoRecord.setUsedTimes(2);
    ecResourceRecordMapper.updateECResourceInfoRecord(ecResourceInfoRecord);
  }

  @Test
  void deleteECResourceInfoRecordByTicketId() {
    insertECResourceInfoRecord();
    ecResourceRecordMapper.deleteECResourceInfoRecordByTicketId(
        "617cea23-0735-4df8-a0a9-703cac91f5e5");
    ECResourceInfoRecord ecResourceInfoRecord =
        ecResourceRecordMapper.getECResourceInfoRecord("617cea23-0735-4df8-a0a9-703cac91f5e5");
    assertTrue(ecResourceInfoRecord == null);
  }

  @Test
  void deleteECResourceInfoRecord() {
    insertECResourceInfoRecord();
    ECResourceInfoRecord select =
        ecResourceRecordMapper.getECResourceInfoRecordByInstance("hadoop001:44835");
    Integer delId = select.getId();
    ecResourceRecordMapper.deleteECResourceInfoRecord(delId);
    ecResourceRecordMapper.deleteECResourceInfoRecordByTicketId(
        "617cea23-0735-4df8-a0a9-703cac91f5e5");
    ECResourceInfoRecord ecResourceInfoRecord =
        ecResourceRecordMapper.getECResourceInfoRecord("617cea23-0735-4df8-a0a9-703cac91f5e5");
    assertTrue(ecResourceInfoRecord == null);
  }

  @Test
  void getECResourceInfoHistory() {
    insertECResourceInfoRecord();
    List<ECResourceInfoRecord> list =
        ecResourceRecordMapper.getECResourceInfoHistory(
            "test", "hadoop001:44835", null, null, "shell-1");
    assertTrue(list.size() >= 1);
  }
}
