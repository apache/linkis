package org.apache.linkis.basedatamanager.server.restful;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.linkis.basedatamanager.server.domain.DatasourceTypeEntity;
import org.apache.linkis.basedatamanager.server.domain.DatasourceTypeKeyEntity;
import org.apache.linkis.basedatamanager.server.service.DatasourceTypeKeyService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/basedata-manager/datasource-type-key")
public class DatasourceTypeKeyRestfulApi {
    private DatasourceTypeKeyService datasourceTypeKeyService;

    public DatasourceTypeKeyRestfulApi(DatasourceTypeKeyService datasourceTypeKeyService) {
        this.datasourceTypeKeyService = datasourceTypeKeyService;
    }

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "string", name = "searchName"),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "currentPage"),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "pageSize")
    })
    @ApiOperation(value = "list", notes = "list Datasource Type Key", httpMethod = "GET")
    @RequestMapping(path = "", method = RequestMethod.GET)
    public Message list(
            HttpServletRequest request, String searchName, Integer currentPage, Integer pageSize) {
        ModuleUserUtils.getOperationUser(
                request, "Query list data of Datasource Type Key,search name:" + searchName);
        PageInfo pageList = datasourceTypeKeyService.getListByPage(searchName, currentPage, pageSize);
        return Message.ok("").data("list", pageList);
    }

    @ApiImplicitParams({@ApiImplicitParam(paramType = "path", dataType = "long", name = "id")})
    @ApiOperation(value = "get", notes = "Get a Datasource Type Key by id", httpMethod = "GET")
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public Message get(HttpServletRequest request, @PathVariable("id") Long id) {
        ModuleUserUtils.getOperationUser(request, "Get a Datasource Type Key Record,id:" + id.toString());
        DatasourceTypeKeyEntity datasourceType = datasourceTypeKeyService.getById(id);
        return Message.ok("").data("item", datasourceType);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(
                    paramType = "body",
                    dataType = "DatasourceTypeEntity",
                    name = "datasourceType")
    })
    @ApiOperation(value = "add", notes = "Add a Datasource Type Key Record", httpMethod = "POST")
    @RequestMapping(path = "", method = RequestMethod.POST)
    public Message add(HttpServletRequest request, @RequestBody DatasourceTypeKeyEntity datasourceType) {
        ModuleUserUtils.getOperationUser(
                request, "Add a Datasource Type Key Record," + datasourceType.toString());
        boolean result = datasourceTypeKeyService.save(datasourceType);
        return Message.ok("").data("result", result);
    }

    @ApiImplicitParams({@ApiImplicitParam(paramType = "path", dataType = "long", name = "id")})
    @ApiOperation(
            value = "remove",
            notes = "Remove a Datasource Type Key Record by id",
            httpMethod = "DELETE")
    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
    public Message remove(HttpServletRequest request, @PathVariable("id") Long id) {
        ModuleUserUtils.getOperationUser(
                request, "Remove a Datasource Type Key Record,id:" + id.toString());
        boolean result = datasourceTypeKeyService.removeById(id);
        return Message.ok("").data("result", result);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(
                    paramType = "body",
                    dataType = "DatasourceTypeEntity",
                    name = "datasourceType")
    })
    @ApiOperation(value = "update", notes = "Update a Datasource Type Key Record", httpMethod = "PUT")
    @RequestMapping(path = "", method = RequestMethod.PUT)
    public Message update(
            HttpServletRequest request, @RequestBody DatasourceTypeKeyEntity datasourceType) {
        ModuleUserUtils.getOperationUser(
                request, "Update a Datasource Type Key Record,id:" + datasourceType.getId().toString());
        boolean result = datasourceTypeKeyService.updateById(datasourceType);
        return Message.ok("").data("result", result);
    }
}
