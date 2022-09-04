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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.linkis.basedatamanager.server.domain.UdfTreeEntity;
import org.apache.linkis.basedatamanager.server.service.UdfTreeService;
import org.apache.linkis.server.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags="UdfTreeRestfulApi")
@RestController
@RequestMapping(path = "/basedata_manager/udf_tree")
public class UdfTreeRestfulApi {

    @Autowired
    UdfTreeService udfTreeService;

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "string", name = "searchName", value = ""),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "currentPage", value = ""),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "pageSize", value = "")
    })
    @ApiOperation(value = "list", notes = "get list data", httpMethod = "GET")
    @RequestMapping(path = "", method = RequestMethod.GET)
    public Message list(String searchName,Integer currentPage,Integer pageSize) {
        PageInfo pageList = udfTreeService.getListByPage(searchName,currentPage,pageSize);
        return Message.ok("").data("list", pageList);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", dataType = "long", name = "id", value = "")
    })
    @ApiOperation(value = "get", notes = "get data by id", httpMethod = "GET")
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public Message get(@PathVariable("id") Long id) {
        UdfTreeEntity errorCode = udfTreeService.getById(id);
        return Message.ok("").data("item", errorCode);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "UdfTreeEntity", name = "errorCode", value = "")
    })
    @ApiOperation(value = "add", notes = "add data", httpMethod = "POST")
    @RequestMapping(path = "", method = RequestMethod.POST)
    public Message add(@RequestBody UdfTreeEntity errorCode) {
        boolean result = udfTreeService.save(errorCode);
        return Message.ok("").data("result", result);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", dataType = "long", name = "id", value = "")
    })
    @ApiOperation(value = "remove", notes = "remove data by id", httpMethod = "DELETE")
    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
    public Message remove(@PathVariable("id") Long id) {
        boolean result = udfTreeService.removeById(id);
        return Message.ok("").data("result", result);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "UdfTreeEntity", name = "errorCode", value = "")
    })
    @ApiOperation(value = "update", notes = "update data", httpMethod = "PUT")
    @RequestMapping(path = "", method = RequestMethod.PUT)
    public Message update(@RequestBody UdfTreeEntity errorCode) {
        boolean result = udfTreeService.updateById(errorCode);
        return Message.ok("").data("result", result);
    }


}
