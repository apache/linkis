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
import org.apache.linkis.basedatamanager.server.domain.DatasourceAccessEntity;
import org.apache.linkis.basedatamanager.server.service.DatasourceAccessService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/basedata-manager/datasource-access")
public class DatasourceAccessRestfulApi {

    @Autowired
    DatasourceAccessService datasourceAccessService;

    @RequestMapping(path = "", method = RequestMethod.GET)
    public Message list(HttpServletRequest request, String searchName, Integer currentPage, Integer pageSize) {
        ModuleUserUtils.getOperationUser(request, "list");
        PageInfo pageList = datasourceAccessService.getListByPage(searchName,currentPage,pageSize);
        return Message.ok("").data("list", pageList);
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public Message get(HttpServletRequest request,@PathVariable("id") Long id) {
        ModuleUserUtils.getOperationUser(request, "get");
        DatasourceAccessEntity datasourceAccess = datasourceAccessService.getById(id);
        return Message.ok("").data("item", datasourceAccess);
    }

    @RequestMapping(path = "", method = RequestMethod.POST)
    public Message add(HttpServletRequest request,@RequestBody DatasourceAccessEntity errorCode) {
        ModuleUserUtils.getOperationUser(request, "add");
        boolean result = datasourceAccessService.save(errorCode);
        return Message.ok("").data("result", result);
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
    public Message remove(HttpServletRequest request,@PathVariable("id") Long id) {
        ModuleUserUtils.getOperationUser(request, "remove");
        boolean result = datasourceAccessService.removeById(id);
        return Message.ok("").data("result", result);
    }

    @RequestMapping(path = "", method = RequestMethod.PUT)
    public Message update(HttpServletRequest request,@RequestBody DatasourceAccessEntity errorCode) {
        ModuleUserUtils.getOperationUser(request, "update");
        boolean result = datasourceAccessService.updateById(errorCode);
        return Message.ok("").data("result", result);
    }


}
