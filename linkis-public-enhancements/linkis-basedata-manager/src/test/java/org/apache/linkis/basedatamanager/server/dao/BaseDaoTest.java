package org.apache.linkis.basedatamanager.server.dao;

import org.apache.linkis.basedatamanager.server.Scan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = Scan.class)
@Transactional
@Rollback()
@EnableTransactionManagement
public abstract class BaseDaoTest {}
