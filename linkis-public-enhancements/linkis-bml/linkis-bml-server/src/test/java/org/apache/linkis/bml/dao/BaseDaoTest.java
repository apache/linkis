package org.apache.linkis.bml.dao;

import org.apache.linkis.bml.Scan;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = Scan.class)
@Transactional
@Rollback(true)
@EnableTransactionManagement
public class BaseDaoTest {}
