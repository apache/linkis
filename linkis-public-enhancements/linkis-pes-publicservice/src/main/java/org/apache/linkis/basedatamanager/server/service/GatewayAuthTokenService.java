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

package org.apache.linkis.basedatamanager.server.service;

import org.apache.linkis.basedatamanager.server.domain.GatewayAuthTokenEntity;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;

/**
 * @description Database operation Service for the [linkis_mg_gateway_auth_token] table
 * @createDate 2022-07-13 10:42:13
 */
public interface GatewayAuthTokenService extends IService<GatewayAuthTokenEntity> {
  PageInfo getListByPage(String searchName, Integer currentPage, Integer pageSize);

  GatewayAuthTokenEntity getEntityByToken(String token);

  GatewayAuthTokenEntity selectTokenBySign(String token);

  List<GatewayAuthTokenEntity> selectTokenByNameWithLike(String token);
}
