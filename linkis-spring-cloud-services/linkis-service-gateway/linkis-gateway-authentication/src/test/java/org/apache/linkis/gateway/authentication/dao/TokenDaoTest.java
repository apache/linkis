package org.apache.linkis.gateway.authentication.dao;

import org.apache.linkis.gateway.authentication.dao.utils.MyBatisUtil;
import org.apache.linkis.gateway.authentication.entity.TokenEntity;

import org.apache.ibatis.session.SqlSession;

import org.junit.Assert;

import java.util.List;

/** Created by shangda on 2021/9/9. */
public class TokenDaoTest {
    @org.junit.Test
    public void selectTokenByName() throws Exception {
        SqlSession session = MyBatisUtil.getSqlSession();
        TokenDao dao = session.getMapper(TokenDao.class);
        List<TokenEntity> list = dao.selectTokenByName("test1");
        for (TokenEntity entity : list) {
            System.out.println(entity.getTokenName());
        }
    }

    @org.junit.Test
    public void getAllTokens() throws Exception {
        SqlSession session = MyBatisUtil.getSqlSession();
        TokenDao dao = session.getMapper(TokenDao.class);
        List<TokenEntity> list = dao.getAllTokens();
        int cnt = 0;
        for (TokenEntity entity : list) {
            System.out.println(entity.getTokenName());
            cnt++;
        }
        Assert.assertEquals(cnt, 10);
    }
}
