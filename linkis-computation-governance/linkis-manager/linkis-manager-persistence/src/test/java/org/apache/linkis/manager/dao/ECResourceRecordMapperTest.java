package org.apache.linkis.manager.dao;

import org.apache.linkis.manager.common.entity.persistence.ECResourceInfoRecord;

import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.util.List;

import org.h2.tools.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ECResourceRecordMapperTest extends BaseDaoTest {

  @Autowired ECResourceRecordMapper ecResourceRecordMapper;

  @BeforeAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void beforeAll() throws Exception {
    Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start();
  }

  @AfterAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void afterAll() throws Exception {}

  private String user = "King";
  private String labelValue = "testLabelValue";
  private String ticketId = "testTicketId";

  private ECResourceInfoRecord insertOne() {
    Date date = new Date(System.currentTimeMillis());
    ECResourceInfoRecord record = new ECResourceInfoRecord();
    record.setCreateUser(user);
    record.setCreateTime(date);
    record.setUsedTime(date);
    record.setLabelValue(labelValue);
    record.setTicketId(ticketId);

    ecResourceRecordMapper.insertECResourceInfoRecord(record);
    return record;
  }

  @Test
  public void testGetECResourceInfoRecord() {
    insertOne();
    ECResourceInfoRecord record = ecResourceRecordMapper.getECResourceInfoRecord(ticketId);
    assertEquals(labelValue, record.getLabelValue());
  }

  @Test
  public void testUpdateECResourceInfoRecord() {
    ECResourceInfoRecord r = insertOne();
    r.setServiceInstance("ttt");
    ECResourceInfoRecord record = ecResourceRecordMapper.getECResourceInfoRecord(ticketId);
    assertNull(record.getServiceInstance());
    ecResourceRecordMapper.updateECResourceInfoRecord(r);
    ECResourceInfoRecord record1 = ecResourceRecordMapper.getECResourceInfoRecord(ticketId);
    assertEquals("ttt", record1.getServiceInstance());
  }

  @Test
  public void testDeleteECResourceInfoRecordByTicketId() {
    insertOne();
    ECResourceInfoRecord record = ecResourceRecordMapper.getECResourceInfoRecord(ticketId);
    assertNotNull(record);
    ecResourceRecordMapper.deleteECResourceInfoRecordByTicketId(ticketId);
    ECResourceInfoRecord record1 = ecResourceRecordMapper.getECResourceInfoRecord(ticketId);
    assertNull(record1);
  }

  @Test
  public void testDeleteECResourceInfoRecord() {
    ECResourceInfoRecord r = insertOne();
    ECResourceInfoRecord record = ecResourceRecordMapper.getECResourceInfoRecord(ticketId);
    assertNotNull(record);
    ecResourceRecordMapper.deleteECResourceInfoRecord(r.getId());
    ECResourceInfoRecord record1 = ecResourceRecordMapper.getECResourceInfoRecord(ticketId);
    assertNull(record1);
  }

  @Test
  public void testGetECResourceInfoHistory() {
    String username = user;
    String enginType = "test";
    ECResourceInfoRecord record = insertOne();

    List<ECResourceInfoRecord> history =
        ecResourceRecordMapper.getECResourceInfoHistory(username, null, null, null, null);
    assertEquals(1, history.size());
    history =
        ecResourceRecordMapper.getECResourceInfoHistory(username, null, null, null, enginType);
    assertEquals(1, history.size());
    history =
        ecResourceRecordMapper.getECResourceInfoHistory(username, "test", null, null, enginType);
    assertEquals(0, history.size());
  }
}
