package org.apache.linkis.configuration.dao;

import org.apache.linkis.configuration.entity.UserIpVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserIpMapperTest extends BaseDaoTest {

    @Autowired
    UserIpMapper userIpMapper;

    UserIpVo insert() {
        UserIpVo userIpVo = new UserIpVo();
        userIpVo.setUser("user");
        userIpVo.setBussinessUser("bussinessUser");
        userIpVo.setCreator("creator");
        userIpVo.setCreateTime(new Date());
        userIpVo.setUpdateTime(new Date());
        userIpVo.setDesc("desc");
        userIpVo.setIpList("ips");
        userIpMapper.createUserIP(userIpVo);
        return userIpVo;
    }

    @Test
    void createUserIP() {
        insert();
        UserIpVo userIpVo = userIpMapper.queryUserIP("user", "creator");
        assertTrue(userIpVo != null);
    }

    @Test
    void deleteUserIP() {
        insert();
        UserIpVo userIpVo = userIpMapper.queryUserIP("user", "creator");
        userIpMapper.deleteUserIP(Integer.valueOf(userIpVo.getId()));
        UserIpVo list = userIpMapper.queryUserIP("user", "creator");
        assertTrue(list == null);
    }

    @Test
    void updateUserIP() {
        insert();
        UserIpVo userIpVo = userIpMapper.queryUserIP("user", "creator");
        UserIpVo updateUserIpVo = new UserIpVo();
        updateUserIpVo.setId(userIpVo.getId());
        updateUserIpVo.setDesc("desc2");
        updateUserIpVo.setBussinessUser("bussinessUser2");
        userIpMapper.updateUserIP(updateUserIpVo);
        UserIpVo userIpVo1 = userIpMapper.queryUserIP("user", "creator");
        assertTrue(userIpVo1.getDesc().equals("desc2"));
        assertTrue(userIpVo1.getBussinessUser().equals("bussinessUser2"));
    }

    @Test
    void queryUserIP() {
        insert();
        UserIpVo userIpVo = userIpMapper.queryUserIP("user", "creator");
        assertTrue(userIpVo != null);
    }

    @Test
    void queryUserIPList() {
        insert();
        List<UserIpVo> userIpVos = userIpMapper.queryUserIPList("user", "creator");
        assertTrue(userIpVos.size() > 0);
    }

}