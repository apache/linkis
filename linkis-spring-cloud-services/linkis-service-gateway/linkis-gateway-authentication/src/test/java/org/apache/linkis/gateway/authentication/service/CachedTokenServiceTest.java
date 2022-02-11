package org.apache.linkis.gateway.authentication.service;

import org.apache.linkis.gateway.authentication.dao.TokenDao;
import org.apache.linkis.gateway.authentication.dao.utils.MyBatisUtil;
import org.apache.linkis.gateway.authentication.entity.TokenEntity;
import org.apache.linkis.gateway.authentication.exception.TokenAuthException;

import org.apache.ibatis.session.SqlSession;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/** Created by shangda on 2021/9/9. */
public class CachedTokenServiceTest {

    @Autowired private CachedTokenService service;

    @Before
    public void doBefore() {
        //        service = new CachedTokenService();
        SqlSession session = MyBatisUtil.getSqlSession();
        TokenDao dao = session.getMapper(TokenDao.class);
        List<TokenEntity> list = dao.getAllTokens();
        for (TokenEntity entity : list) {
            System.out.println(entity.getTokenName());
        }
        //        service.setTokenDao(dao);
        //        service.init();
    }

    @Test
    public void isTokenAcceptableWithHost() throws Exception {
        if (service != null) {
            Assert.assertTrue(service.isTokenAcceptableWithHost("test1", "127.0.0.1"));
            Assert.assertFalse(service.isTokenAcceptableWithHost("test1", "127.0.0.16"));
            Assert.assertTrue(service.isTokenAcceptableWithHost("test3", "127.0.0.1"));
        }
    }

    @Test
    public void doAuth() throws Exception {
        if (service != null) {
            Assert.assertTrue(service.doAuth("test1", "hduser05", "127.0.0.1"));
            Assert.assertTrue(service.doAuth("test2", "hduser05", "127.0.0.1"));
            Assert.assertTrue(service.doAuth("test3", "hduser05", "127.0.0.1"));
            try {
                service.doAuth("test1", "hduser05", "127.0.0.16");
            } catch (Exception e) {
                Assert.assertEquals(((TokenAuthException) e).getErrCode(), 15203);
            }
            try {
                service.doAuth("test2", "hduser05", "127.0.0.16");
            } catch (Exception e) {
                Assert.assertEquals(((TokenAuthException) e).getErrCode(), 15203);
            }
            Assert.assertTrue(service.doAuth("test3", "hduser05", "127.0.0.16"));
            try {
                service.doAuth("test1", "hduser055", "127.0.0.1");
            } catch (Exception e) {
                Assert.assertEquals(((TokenAuthException) e).getErrCode(), 15202);
            }
            Assert.assertTrue(service.doAuth("test2", "hduser055", "127.0.0.1"));
            try {
                service.doAuth("test3", "hduser055", "127.0.0.1");
            } catch (Exception e) {
                Assert.assertEquals(((TokenAuthException) e).getErrCode(), 15202);
            }
            try {
                service.doAuth("test4", "hduser05", "127.0.0.1");
            } catch (Exception e) {
                Assert.assertEquals(((TokenAuthException) e).getErrCode(), 15201);
            }
            try {
                service.doAuth("xxxx", "hduser05", "127.0.0.1");
            } catch (Exception e) {
                Assert.assertEquals(((TokenAuthException) e).getErrCode(), 15201);
            }
        }
    }

    @Test
    public void isTokenAcceptableWithUser() throws Exception {
        if (service != null) {
            Assert.assertTrue(service.isTokenAcceptableWithUser("test1", "hduser05"));
            Assert.assertFalse(service.isTokenAcceptableWithUser("test1", "hduser055"));
            Assert.assertTrue(service.isTokenAcceptableWithUser("test2", "hduser05"));
        }
    }

    @Test
    public void isTokenValid() throws Exception {
        if (service != null) {
            Assert.assertTrue(service.isTokenValid("test1"));
            Assert.assertFalse(service.isTokenValid("test4"));
            Assert.assertFalse(service.isTokenValid("xxx"));
        }
    }
}
