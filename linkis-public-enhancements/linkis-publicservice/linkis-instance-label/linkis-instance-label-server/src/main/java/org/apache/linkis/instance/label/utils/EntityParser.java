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
 
package org.apache.linkis.instance.label.utils;

import org.apache.linkis.instance.label.entity.InstanceInfo;
import org.apache.linkis.instance.label.vo.InstanceInfoVo;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;


public class EntityParser {

    public static InstanceInfoVo parseToInstanceVo(InstanceInfo instanceInfo){
        InstanceInfoVo instanceInfoVo = new InstanceInfoVo();
        if(instanceInfo != null){
            BeanUtils.copyProperties(instanceInfo, instanceInfoVo);
        }
        return instanceInfoVo;
    }
    public static List<InstanceInfoVo> parseToInstanceVo(List<InstanceInfo> instanceInfos){
        ArrayList<InstanceInfoVo> instanceInfoVos = new ArrayList<>();
        if(!instanceInfos.isEmpty()){
            instanceInfos.stream().forEach(instanceInfo -> {
                InstanceInfoVo instanceInfoVo = parseToInstanceVo(instanceInfo);
                instanceInfoVos.add(instanceInfoVo);
            });
        }
        return instanceInfoVos;
    }
}
