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

package org.apache.linkis.udf.service.impl;

import org.apache.linkis.udf.dao.UDFTreeDao;
import org.apache.linkis.udf.entity.UDFTree;
import org.apache.linkis.udf.excepiton.UDFException;
import org.apache.linkis.udf.service.UDFService;
import org.apache.linkis.udf.service.UDFTreeService;
import org.apache.linkis.udf.utils.ConstantVar;
import org.apache.linkis.udf.vo.UDFInfoVo;

import org.apache.commons.collections.map.HashedMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.*;

import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.udf.utils.ConstantVar.*;

@Service
public class UDFTreeServiceImpl implements UDFTreeService {

  private static final Logger logger = LoggerFactory.getLogger(UDFTreeServiceImpl.class);

  @Autowired private UDFTreeDao udfTreeDao;
  @Autowired private UDFService udfService;

  Map<String, List<UDFTree>> firstFloor = new HashMap<>();
  Map<String, String> firstFloorName = new HashedMap();

  {
    firstFloorName.put(SYS_USER, "系统函数");
    firstFloorName.put(BDP_USER, "BDAP函数");
    firstFloorName.put(SHARE_USER, "共享函数");
    firstFloorName.put(EXPIRE_USER, "过期函数");
  }

  /**
   * 每个用户的初始化，以及第一次调用系统的初始化
   *
   * @param userName
   * @param category
   * @return
   * @throws UDFException
   */
  @Override
  public UDFTree initTree(String userName, String category) throws UDFException {
    List<UDFTree> childrens = new ArrayList<>();
    if (firstFloor.get(category) == null || firstFloor.get(category).size() != 4) {
      List<UDFTree> root = new ArrayList<>();
      // sys
      root.add(0, getFirstFloor(SYS_USER, category));
      // bdp
      root.add(1, getFirstFloor(BDP_USER, category));
      // shared
      root.add(2, getFirstFloor(SHARE_USER, category));
      // expired
      root.add(3, getFirstFloor(EXPIRE_USER, category));

      firstFloor.put(category, root);
    }
    for (int i = 0; i < 4; i++) {
      childrens.add(firstFloor.get(category).get(i));
    }
    childrens.add(getFirstFloor(userName, category));
    UDFTree udfTree = new UDFTree();
    udfTree.setChildrens(childrens);
    return udfTree;
  }

  /**
   * Get the user's first level directory(获得用户的第一层目录)
   *
   * @param userName
   * @param category
   * @return
   * @throws UDFException
   */
  public UDFTree getFirstFloor(String userName, String category) throws UDFException {
    logger.info(userName + " to get first Floor directory");
    Map<String, Object> params = new HashedMap();
    params.put("parent", -1L);
    params.put("userName", userName);
    params.put("category", category);
    List<UDFTree> first = udfTreeDao.getTreesByParentId(params);
    if (first == null || first.size() == 0) {
      String treeName = firstFloorName.getOrDefault(userName, "个人函数");
      UDFTree udfTree =
          new UDFTree(null, -1L, treeName, userName, "", new Date(), new Date(), category);
      udfTreeDao.addTree(udfTree);
      return udfTree;
    }
    if (first.size() > 1) {
      throw new UDFException(
          "user(用户):" + userName + ",There are two root directory directories(存在两个根目录目录)");
    }
    return first.get(0);
  }

  @Override
  public UDFTree addTree(UDFTree udfTree, String userName) throws UDFException {
    if (userName.equals(udfTree.getUserName())) {
      try {
        UDFTree parentTree = udfTreeDao.getTreeById(udfTree.getParent());
        if (parentTree != null && !parentTree.getUserName().equals(userName)) {
          throw new UDFException(
              "user(用户) " + userName + ", the parent directory is not yours(父目录不是你的)");
        }

        logger.info(userName + " to add directory");
        udfTreeDao.addTree(udfTree);
      } catch (Throwable e) {
        if (e instanceof DuplicateKeyException) {
          throw new UDFException("Duplicate file name(文件名重复)");
        } else {
          throw new UDFException(e.getMessage());
        }
      }

    } else {
      throw new UDFException(
          "Current user must be consistent with the user created(当前用户必须和创建用户一致)");
    }
    return udfTree;
  }

  @Override
  public UDFTree updateTree(UDFTree udfTree, String userName) throws UDFException {
    if (userName.equals(udfTree.getUserName())) {
      if (udfTree.getId() == null) {
        throw new UDFException("id Can not be empty(不能为空)");
      }
      try {
        logger.info(userName + " to update directory");
        udfTreeDao.updateTree(udfTree);
      } catch (Throwable e) {
        if (e instanceof DuplicateKeyException) {
          throw new UDFException("Duplicate file name(文件名重复)");
        } else {
          throw new UDFException(e.getMessage());
        }
      }
    } else {
      throw new UDFException(
          "Current user must be consistent with the modified user(当前用户必须和修改用户一致)");
    }
    return udfTree;
  }

  @Override
  public Boolean deleteTree(Long id, String userName) throws UDFException {

    udfTreeDao.deleteTree(id, userName);
    return true;
  }

  /**
   * Get UDFs and subdirectories under a layer of directories(获得一层目录下面的UDF和子目录)
   *
   * @param id
   * @param userName
   * @param type
   * @param category
   * @return
   * @throws UDFException
   */
  @Override
  public UDFTree getTreeById(Long id, String userName, String type, String category)
      throws UDFException {
    UDFTree udfTree = null;
    if (id == null || id < 0) {
      udfTree = initTree(userName, category);
    } else {
      if (Arrays.asList(ConstantVar.specialTypes).contains(type)) {
        udfTree = udfTreeDao.getTreeByIdAndCategory(id, category);
      } else {
        udfTree = udfTreeDao.getTreeByIdAndCategoryAndUserName(id, category, userName);
      }

      if (udfTree == null) {
        return udfTree;
      }
      Map<String, Object> params = new HashedMap();
      params.put("parent", udfTree.getId());
      params.put("category", category);
      // TODO Determine if the user can list(判断用户是否可以list)
      List<UDFTree> childrens = udfTreeDao.getTreesByParentId(params);
      List<UDFInfoVo> udfInfos = null;
      switch (type) {
        case SYS_USER:
          udfInfos = udfService.getUDFSByTreeIdAndUser(udfTree.getId(), type, category);
          break;
        case BDP_USER:
          udfInfos = udfService.getUDFInfoByTreeId(udfTree.getId(), userName, category);
          break;
          // case "self": udfInfos =  udfService.getUDFInfoByTreeId(udfTree.getId(),
          // userName); break;
        case SHARE_USER:
          udfInfos = udfService.getSharedUDFs(userName, category);
          break;
        case EXPIRE_USER:
          udfInfos = udfService.getExpiredUDFs(userName, category);
          break;
        default:
          udfInfos = udfService.getUDFInfoByTreeId(udfTree.getId(), userName, category);
          break;
      }
      udfTree.setChildrens(childrens);
      udfTree.setUdfInfos(udfInfos);
    }
    return udfTree;
  }

  @Override
  @Deprecated
  public UDFTree getSharedTree(String category) throws UDFException {
    Map<String, Object> params = new HashedMap();
    params.put("parent", -1L);
    params.put("userName", SHARE_USER);
    params.put("category", category);
    return Iterables.getFirst(udfTreeDao.getTreesByParentId(params), null);
  }
}
