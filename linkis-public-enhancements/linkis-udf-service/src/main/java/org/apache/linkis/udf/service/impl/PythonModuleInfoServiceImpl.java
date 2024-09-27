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

import org.apache.linkis.udf.dao.PythonModuleInfoMapper;
import org.apache.linkis.udf.entity.PythonModuleInfo;
import org.apache.linkis.udf.service.PythonModuleInfoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PythonModuleInfoServiceImpl implements PythonModuleInfoService {

  private final PythonModuleInfoMapper pythonModuleInfoMapper;

  @Autowired
  public PythonModuleInfoServiceImpl(PythonModuleInfoMapper pythonModuleInfoMapper) {
    this.pythonModuleInfoMapper = pythonModuleInfoMapper;
  }

  @Override
  public List<PythonModuleInfo> getByConditions(PythonModuleInfo pythonModuleInfo) {
    return pythonModuleInfoMapper.selectByConditions(pythonModuleInfo);
  }

  @Override
  public int updatePythonModuleInfo(PythonModuleInfo pythonModuleInfo) {
    return pythonModuleInfoMapper.updatePythonModuleInfo(pythonModuleInfo);
  }

  @Override
  public Long insertPythonModuleInfo(PythonModuleInfo pythonModuleInfo) {
    return pythonModuleInfoMapper.insertPythonModuleInfo(pythonModuleInfo);
  }

  @Override
  public PythonModuleInfo getByUserAndNameAndId(PythonModuleInfo pythonModuleInfo) {
    return pythonModuleInfoMapper.selectByUserAndNameAndId(pythonModuleInfo);
  }

  @Override
  public List<PythonModuleInfo> getPathsByUsernameAndEnginetypes(
      String username, List<String> enginetypes) {
    return pythonModuleInfoMapper.selectPathsByUsernameAndEnginetypes(username, enginetypes);
  }
}
