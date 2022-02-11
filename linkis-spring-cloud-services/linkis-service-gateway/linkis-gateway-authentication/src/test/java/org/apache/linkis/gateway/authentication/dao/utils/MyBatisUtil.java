package org.apache.linkis.gateway.authentication.dao.utils;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * MyBatis工具类
 *
 * @author 大强
 */
public class MyBatisUtil {

    public static SqlSessionFactory sqlSessionFactory;

    static {
        try {
            String resource = "src/test/resources/conf/mybatis-config.xml";
            InputStream inputStream = new FileInputStream(new File(resource));
            // 由 SqlSessionFactoryBuilder创建SqlSessionFactory
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 由 SqlSessionFactory创建SqlSession
     *
     * @return
     */
    public static SqlSession getSqlSession() {
        return sqlSessionFactory.openSession();
    }

    /**
     * 关闭SqlSession
     *
     * @param sqlSession
     */
    public static void closeSqlSession(SqlSession sqlSession) {
        if (sqlSession != null) {
            sqlSession.close();
        }
    }

}