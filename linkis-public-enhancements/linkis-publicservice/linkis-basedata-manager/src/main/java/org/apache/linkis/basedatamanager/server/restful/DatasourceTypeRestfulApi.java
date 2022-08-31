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
package org.apache.linkis.basedatamanager.server.restful;

import com.github.pagehelper.PageInfo;
import org.apache.linkis.basedatamanager.server.domain.DatasourceTypeEntity;
import org.apache.linkis.basedatamanager.server.service.DatasourceTypeService;
import org.apache.linkis.server.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/basedata_manager/datasource_type")
public class DatasourceTypeRestfulApi {

    @Autowired
    DatasourceTypeService datasourceTypeService;

    @RequestMapping(path = "", method = RequestMethod.GET)
    public Message list(String searchName,Integer currentPage,Integer pageSize) {
        PageInfo pageList = datasourceTypeService.getListByPage(searchName,currentPage,pageSize);
        return Message.ok("").data("list", pageList);
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public Message get(@PathVariable("id") Long id) {
        DatasourceTypeEntity datasourceType = datasourceTypeService.getById(id);
        return Message.ok("").data("item", datasourceType);
    }

    @RequestMapping(path = "", method = RequestMethod.POST)
    public Message add(@RequestBody DatasourceTypeEntity datasourceType) {
        boolean result = datasourceTypeService.save(datasourceType);
        return Message.ok("").data("result", result);
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
    public Message remove(@PathVariable("id") Long id) {
        boolean result = datasourceTypeService.removeById(id);
        return Message.ok("").data("result", result);
    }

    @RequestMapping(path = "", method = RequestMethod.PUT)
    public Message update(@RequestBody DatasourceTypeEntity errorCode) {
        boolean result = datasourceTypeService.updateById(errorCode);
        return Message.ok("").data("result", result);
    }


}
