/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.filesystem.restful.remote;

import com.webank.wedatasphere.linkis.filesystem.exception.WorkSpaceException;
import org.codehaus.jackson.JsonNode;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

/**
 * Created by johnnwang on 2018/10/24.
 */
public interface FsRestfulRemote {

    @GetMapping("/api/filesystem/getUserRootPath")
    Response getUserRootPath(@Context HttpServletRequest req,@QueryParam("pathType")String pathType) throws IOException, WorkSpaceException;

    @PostMapping("/api/filesystem/createNewDir")
    Response createNewDir(@Context HttpServletRequest req, JsonNode json) throws IOException, WorkSpaceException;

    @PostMapping("/api/filesystem/createNewFile")
    Response createNewFile(@Context HttpServletRequest req, JsonNode json) throws IOException, WorkSpaceException;

    @PostMapping("/api/filesystem/raname")
    Response rename(@Context HttpServletRequest req, JsonNode json) throws IOException, WorkSpaceException;

    @PostMapping("/api/filesystem/upload")
    Response upload(@Context HttpServletRequest req , @FormDataParam("path") String path, FormDataMultiPart form) throws IOException, WorkSpaceException;

    @PostMapping("/api/filesystem/deleteDirOrFile")
    Response deleteDirOrFile(@Context HttpServletRequest req, JsonNode json) throws IOException, WorkSpaceException;

    @GetMapping("/api/filesystem/getFileSystemTree")
    Response getDirFileTrees(@Context HttpServletRequest req, @QueryParam("path")String path) throws IOException, WorkSpaceException;

    @PostMapping("/api/filesystem/download")
    void download(@Context HttpServletRequest req, @Context HttpServletResponse response, @RequestBody Map<String,String> json) throws IOException, WorkSpaceException;

    @PostMapping("/api/filesystem/resultdownload")
    void resultDownload(@Context HttpServletRequest req, @Context HttpServletResponse response, @RequestBody Map<String,String> json) throws IOException, WorkSpaceException;

    @GetMapping("/api/filesystem/isExit")
    Response isExist(@Context HttpServletRequest req, @QueryParam("path")String path) throws IOException, WorkSpaceException;

    @GetMapping("/api/filesystem/openFile")
    Response openFile(@Context HttpServletRequest req, @QueryParam("path")String path,@QueryParam("page")Integer page,@QueryParam("pageSize") Integer pageSize,
                      @QueryParam("charset")String charset) throws IOException, WorkSpaceException;

    @PostMapping("/api/filesystem/saveScript")
    Response saveScript(@Context HttpServletRequest req,@RequestBody Map<String,Object> json) throws IOException, WorkSpaceException;

    @GetMapping("/api/filesystem/resultsetToExcel")
    void resultsetToExcel(
                             @Context HttpServletRequest req,
                             @Context HttpServletResponse response,
                             @QueryParam("path")String path,@QueryParam("charset") String charset,
                             @QueryParam("outputFileType")String outputFileType,
                             @QueryParam("outputFileName")String outputFileName) throws WorkSpaceException, IOException;
    @GetMapping("/api/filesystem/formate")
    Response formate(@Context HttpServletRequest req,
                     @QueryParam("path") String path,
                     @QueryParam("encoding") String encoding,
                     @QueryParam("fieldDelimiter") String fieldDelimiter,
                     @QueryParam("hasHeader") Boolean hasHeader,
                     @QueryParam("quote") String quote,
                     @QueryParam("escapeQuotes") Boolean escapeQuotes) throws Exception;

    @GetMapping("/api/filesystem/openLog")
    Response openLog(@Context HttpServletRequest req, @QueryParam("path")String path) throws IOException, WorkSpaceException;
}
