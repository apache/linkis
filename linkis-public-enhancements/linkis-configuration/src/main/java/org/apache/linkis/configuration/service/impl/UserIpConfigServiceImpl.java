/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.linkis.configuration.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.linkis.configuration.dao.UserIpMapper;
import org.apache.linkis.configuration.entity.UserIpVo;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.configuration.service.UserIpConfigService;
import org.apache.linkis.configuration.util.CommonUtils;
import org.apache.linkis.server.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

@Service
public class UserIpConfigServiceImpl implements UserIpConfigService {

    @Autowired
    private UserIpMapper userIpMapper;

    /***
     * 创建IP数据
     * @param userIpVo
     * @return
     */
    @Override
    public Message createUserIP(UserIpVo userIpVo) {
        try {
            dataProcessing(userIpVo);
            userIpVo.setCreateTime(new Date());
            userIpVo.setUpdateTime(new Date());
            userIpMapper.createUserIP(userIpVo);
        } catch (DuplicateKeyException e) {
            return Message.error("create user-creator is existed");
        } catch (ConfigurationException e) {
            return Message.error(e.getMessage());
        }
        return Message.ok();
    }

    /**
     * 更新IP数据
     *
     * @param userIpVo
     */
    @Override
    public Message updateUserIP(UserIpVo userIpVo) {
        try {
            if (StringUtils.isBlank(userIpVo.getId())) {
                throw new ConfigurationException("id couldn't be empty ");
            }
            dataProcessing(userIpVo);
            userIpVo.setUpdateTime(new Date());
            userIpMapper.updateUserIP(userIpVo);
        } catch (ConfigurationException e) {
            return Message.error(e.getMessage());
        }
        return Message.ok();
    }

    /**
     * 删除IP
     *
     * @param id
     */
    @Override
    public void deleteUserIP(Integer id) {
        userIpMapper.deleteUserIP(id);
    }

    /**
     * 查询IP集合
     *
     * @return List<UserIpVo>
     */
    @Override
    public List<UserIpVo> queryUserIPList() {
        return userIpMapper.queryUserIPList();
    }

    @Override
    public Message queryUserIP(UserIpVo userIpVo) {
        //参数校验
        if (StringUtils.isBlank(userIpVo.getCreator())) {
            return Message.error("creator couldn't be empty ");
        }
        if (StringUtils.isBlank(userIpVo.getUser())) {
            return Message.error("user couldn't be empty ");
        }
        UserIpVo userIp = userIpMapper.queryUserIP(userIpVo.getUser(), userIpVo.getCreator());
        if (ObjectUtils.isEmpty(userIp)) {
            return Message.error("No data found");
        }
        return Message.ok().data("userIp", userIp);
    }

    private void dataProcessing(UserIpVo userIpVo) throws ConfigurationException {
        //参数校验
        if (StringUtils.isBlank(userIpVo.getCreator())) {
            throw new ConfigurationException("creator couldn't be empty ");
        }
        if (StringUtils.isBlank(userIpVo.getUser())) {
            throw new ConfigurationException("user couldn't be empty ");
        }
        if (StringUtils.isBlank(userIpVo.getBussinessUser())) {
            throw new ConfigurationException("bussiness_user couldn't be empty ");
        }
        if (StringUtils.isBlank(userIpVo.getIpList())) {
            throw new ConfigurationException("ipList couldn't be empty ");
        }
        if (StringUtils.isBlank(userIpVo.getDesc())) {
            throw new ConfigurationException("desc couldn't be empty ");
        }
        //ip规则校验
        String[] split = userIpVo.getIpList().split(",");
        StringJoiner joiner = new StringJoiner(",");
        Arrays.stream(split).filter(ipStr -> !CommonUtils.ipCheck(ipStr)).forEach(joiner::add);
        if (StringUtils.isNotBlank(joiner.toString())) {
            throw new ConfigurationException(joiner + ",Illegal IP address ");
        }
    }

}
