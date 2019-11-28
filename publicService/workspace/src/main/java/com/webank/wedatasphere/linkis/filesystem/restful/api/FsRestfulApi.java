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

package com.webank.wedatasphere.linkis.filesystem.restful.api;


import com.webank.wedatasphere.linkis.common.io.FsPath;
import com.webank.wedatasphere.linkis.common.io.MetaData;
import com.webank.wedatasphere.linkis.common.io.Record;
import com.webank.wedatasphere.linkis.common.io.resultset.ResultSet;
import com.webank.wedatasphere.linkis.filesystem.conf.WorkSpaceConfiguration;
import com.webank.wedatasphere.linkis.filesystem.entity.DirFileTree;
import com.webank.wedatasphere.linkis.filesystem.exception.WorkSpaceException;
import com.webank.wedatasphere.linkis.filesystem.restful.remote.FsRestfulRemote;
import com.webank.wedatasphere.linkis.filesystem.service.FsService;
import com.webank.wedatasphere.linkis.filesystem.util.Constants;
import com.webank.wedatasphere.linkis.filesystem.util.WorkspaceUtil;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import com.webank.wedatasphere.linkis.storage.FSFactory;
import com.webank.wedatasphere.linkis.storage.LineMetaData;
import com.webank.wedatasphere.linkis.storage.LineRecord;
import com.webank.wedatasphere.linkis.storage.csv.CSVFsWriter;
import com.webank.wedatasphere.linkis.storage.domain.Column;
import com.webank.wedatasphere.linkis.storage.domain.FsPathListWithError;
import com.webank.wedatasphere.linkis.storage.excel.ExcelFsWriter;
import com.webank.wedatasphere.linkis.storage.excel.ExcelStorageReader;
import com.webank.wedatasphere.linkis.storage.fs.FileSystem;
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetFactory;
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetFactory$;
import com.webank.wedatasphere.linkis.storage.resultset.ResultSetReader;
import com.webank.wedatasphere.linkis.storage.resultset.table.TableMetaData;
import com.webank.wedatasphere.linkis.storage.resultset.table.TableRecord;
import com.webank.wedatasphere.linkis.storage.script.*;
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.codehaus.jackson.JsonNode;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *  johnnwang
 *  2018/10/25
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON, MediaType.MULTIPART_FORM_DATA})
@Component
@Path("filesystem")
public class FsRestfulApi implements FsRestfulRemote {
    @Autowired
    private FsService fsService;

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private FileSystem getFileSystem(FsPath fsPath, String userName) throws IOException {
        FileSystem fileSystem = (FileSystem) FSFactory.getFsByProxyUser(fsPath, userName);
        fileSystem.init(new HashMap<>());
        return fileSystem;
    }

    private void fsValidate(FileSystem fileSystem) throws WorkSpaceException {
        if (fileSystem == null){
            throw new WorkSpaceException("The user has obtained the filesystem for more than 2s. Please contact the administrator.（用户获取filesystem的时间超过2s，请联系管理员）");
        }
    }

    @GET
    @Path("/getUserRootPath")
    @Override
    public Response getUserRootPath(@Context HttpServletRequest req,@QueryParam("pathType")String pathType) throws IOException, WorkSpaceException {
        String userName = SecurityFilter.getLoginUsername(req);
        String path = null;
        String returnType = null;
        if(pathType.equals("hdfs")){
            if (WorkSpaceConfiguration.HDFS_USER_ROOT_PATH_PREFIX.getValue().toString().endsWith("/")){
                path = WorkSpaceConfiguration.HDFS_USER_ROOT_PATH_PREFIX.getValue() + userName + WorkSpaceConfiguration.HDFS_USER_ROOT_PATH_SUFFIX.getValue();
            }else{
                path = WorkSpaceConfiguration.HDFS_USER_ROOT_PATH_PREFIX.getValue() + "/" +  userName + WorkSpaceConfiguration.HDFS_USER_ROOT_PATH_SUFFIX.getValue();
            }
            returnType = "HDFS";
        }else {
            if (WorkSpaceConfiguration.LOCAL_USER_ROOT_PATH.getValue().toString().endsWith("/")){
                path = WorkSpaceConfiguration.LOCAL_USER_ROOT_PATH.getValue() + userName + "/";
            }else{
                path = WorkSpaceConfiguration.LOCAL_USER_ROOT_PATH.getValue() + "/" + userName + "/";
            }
            returnType = "Local";
        }
        FsPath fsPath = new FsPath(path);
        FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
        if (fileSystem != null &&!fileSystem.exists(fsPath)) {
            throw new WorkSpaceException("User local root directory does not exist, please contact administrator to add（用户本地根目录不存在,请联系管理员添加)");
        }
        if (fileSystem == null) {path = null;}
        return Message.messageToResponse(Message.ok().data("user"+returnType+"RootPath", path));
    }

    @POST
    @Path("/createNewDir")
    @Override
    public Response createNewDir(@Context HttpServletRequest req, JsonNode json) throws IOException, WorkSpaceException {
        String userName = SecurityFilter.getLoginUsername(req);
        String path = json.get("path").getTextValue();
        if (StringUtils.isEmpty(path)) {
            throw new WorkSpaceException("path:(路径：)" + path + "Is empty!(为空！)");
        }
        WorkspaceUtil.pathSafeCheck(path,userName);
        WorkspaceUtil.fileAndDirNameSpecialCharCheck(path);
        FsPath fsPath = new FsPath(path);
        FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
        fsValidate(fileSystem);
        if (fileSystem.exists(fsPath)) {
            throw new WorkSpaceException("The created folder name is duplicated!(创建的文件夹名重复!)");
        }
        fileSystem.mkdirs(fsPath);
        return Message.messageToResponse(Message.ok());
    }

    @POST
    @Path("/createNewFile")
    @Override
    public Response createNewFile(@Context HttpServletRequest req, JsonNode json) throws IOException, WorkSpaceException {
        String userName = SecurityFilter.getLoginUsername(req);
        String path = json.get("path").getTextValue();
        if (StringUtils.isEmpty(path)) {
            throw new WorkSpaceException("Path(路径)：" + path + "Is empty!(为空！)");
        }
        WorkspaceUtil.pathSafeCheck(path,userName);
        FsPath fsPath = new FsPath(path);
        FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
        fsValidate(fileSystem);
        if (fileSystem.exists(fsPath)) {
            throw new WorkSpaceException("The file name created is duplicated!(创建的文件名重复!)");
        }
        fileSystem.createNewFile(fsPath);
        return Message.messageToResponse(Message.ok());
    }

    @POST
    @Path("/rename")
    @Override
    public Response rename(@Context HttpServletRequest req, JsonNode json) throws IOException, WorkSpaceException {
        String oldDest = json.get("oldDest").getTextValue();
        String newDest = json.get("newDest").getTextValue();
        String userName = SecurityFilter.getLoginUsername(req);
        if (StringUtils.isEmpty(oldDest)) {
            throw new WorkSpaceException("Path(路径)：" + oldDest + "Is empty!(为空！)");
        }
        WorkspaceUtil.pathSafeCheck(oldDest,userName);
        WorkspaceUtil.pathSafeCheck(newDest,userName);
        WorkspaceUtil.fileAndDirNameSpecialCharCheck(newDest);
        if (StringUtils.isEmpty(newDest)) {
            //No change in file name(文件名字无变化)
            return Message.messageToResponse(Message.ok());
        }
        FsPath fsPathOld = new FsPath(oldDest);
        FsPath fsPathNew = new FsPath(newDest);
        FileSystem fileSystem = fsService.getFileSystem(userName, fsPathOld);
        fsValidate(fileSystem);
        if (fileSystem.exists(fsPathNew)) {
            throw new WorkSpaceException("The renamed name is repeated!(重命名的名字重复!)");
        }
        fileSystem.renameTo(fsPathOld, fsPathNew);
        return Message.messageToResponse(Message.ok());
    }

    @POST
    @Path("/upload")
    @Override
    public Response upload(@Context HttpServletRequest req,
                           @FormDataParam("path") String path,
                           FormDataMultiPart form) throws IOException, WorkSpaceException {
        String userName = SecurityFilter.getLoginUsername(req);
        if (StringUtils.isEmpty(path)) {
            throw new WorkSpaceException("Path(路径)：" + path + "Is empty!(为空！)");
        }
        WorkspaceUtil.pathSafeCheck(path,userName);
        FsPath fsPath = new FsPath(path);
        FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
        fsValidate(fileSystem);
        List<FormDataBodyPart> files = form.getFields("file");
        for (FormDataBodyPart p : files) {
            InputStream is = p.getValueAs(InputStream.class);
            FormDataContentDisposition fileDetail = p.getFormDataContentDisposition();
            String fileName = new String(fileDetail.getFileName().getBytes("ISO8859-1"), "UTF-8");
            FsPath fsPathNew = new FsPath(fsPath.getPath() + "/" + fileName);
            fileSystem.createNewFile(fsPathNew);
            OutputStream outputStream = fileSystem.write(fsPathNew, true);
            IOUtils.copy(is, outputStream);
            StorageUtils.close(outputStream, is, null);
        }
        return Message.messageToResponse(Message.ok());
    }

    @POST
    @Path("/deleteDirOrFile")
    @Override
    public Response deleteDirOrFile(@Context HttpServletRequest req, JsonNode json) throws IOException, WorkSpaceException {
        String userName = SecurityFilter.getLoginUsername(req);
        String path = json.get("path").getTextValue();
        if (StringUtils.isEmpty(path)) {
            throw new WorkSpaceException("Path(路径)：" + path + "Is empty!(为空！)");
        }
        WorkspaceUtil.pathSafeCheck(path,userName);
        FsPath fsPath = new FsPath(path);
        FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
        fsValidate(fileSystem);
        if (!fileSystem.exists(fsPath)) {
            throw new WorkSpaceException("The deleted file or folder does not exist!(删除的文件or文件夹不存在!)");
        }
        if (!fileSystem.canWrite(fsPath.getParent()) || !fileSystem.canExecute(fsPath.getParent())) {
            throw new WorkSpaceException("This user does not have permission to delete this file or folder!(该用户无权删除此文件或文件夹！)");
        }
        deleteAllFiles(fileSystem, fsPath);
        return Message.messageToResponse(Message.ok());
    }

    private boolean isInUserWorkspace(String path,String userName){
        String hdfsPath = WorkSpaceConfiguration.HDFS_USER_ROOT_PATH_PREFIX.getValue() + userName + WorkSpaceConfiguration.HDFS_USER_ROOT_PATH_SUFFIX.getValue();
        hdfsPath = hdfsPath.endsWith("/")?hdfsPath.substring(0,hdfsPath.length() -1):hdfsPath;
        String filePath = WorkSpaceConfiguration.LOCAL_USER_ROOT_PATH.getValue() + userName;
        return path.startsWith(filePath) || path.startsWith(hdfsPath);
    }

    @GET
    @Path("/getDirFileTrees")
    @Override
    public Response getDirFileTrees(@Context HttpServletRequest req,
                                    @QueryParam("path") String path) throws IOException, WorkSpaceException {
        String userName = SecurityFilter.getLoginUsername(req);
        if (StringUtils.isEmpty(path)) {
            throw new WorkSpaceException("Path(路径)：" + path + "Is empty!(为空！)");
        }
        WorkspaceUtil.pathSafeCheck(path,userName);
        FsPath fsPath = new FsPath(path);
        FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
        fsValidate(fileSystem);
        if (!fileSystem.exists(fsPath)) {
            return Message.messageToResponse(Message.ok().data("dirFileTrees", null));
        }
        DirFileTree dirFileTree = new DirFileTree();
        dirFileTree.setPath(fsPath.getSchemaPath());
        //if(!isInUserWorkspace(path,userName)) throw new WorkSpaceException("The user does not have permission to view the contents of the directory");
        if (!fileSystem.canExecute(fsPath) || !fileSystem.canRead(fsPath)) {
            throw new WorkSpaceException("The user does not have permission to view the contents of the directory(该用户无权限查看该目录的内容)");
        }
        dirFileTree.setName(new File(path).getName());
        dirFileTree.setChildren(new ArrayList<>());
        FsPathListWithError fsPathListWithError = fileSystem.listPathWithError(fsPath);
        if (fsPathListWithError != null) {
            for (FsPath children : fsPathListWithError.getFsPaths()) {
                DirFileTree dirFileTreeChildren = new DirFileTree();
                dirFileTreeChildren.setName(new File(children.getPath()).getName());
                dirFileTreeChildren.setPath(fsPath.getFsType() + "://" + children.getPath());
                dirFileTreeChildren.setProperties(new HashMap<>());
                dirFileTreeChildren.setParentPath(fsPath.getSchemaPath());
                if (!children.isdir()) {
                    dirFileTreeChildren.setIsLeaf(true);
                    dirFileTreeChildren.getProperties().put("size", String.valueOf(children.getLength()));
                    dirFileTreeChildren.getProperties().put("modifytime", String.valueOf(children.getModification_time()));
                }
                dirFileTree.getChildren().add(dirFileTreeChildren);
            }
        }
        Message message = Message.ok();
        /*if (fsPathListWithError != null &&!StringUtils.isEmpty(fsPathListWithError.getError())){
            message.data("msg", fsPathListWithError.getError());
        }*/
        message.data("dirFileTrees", dirFileTree);
        return Message.messageToResponse(message);
    }

    /**
     * @param req
     * @param response
     * @param json
     * @throws IOException
     */
    @POST
    @Path("/download")
    @Override
    public void download(@Context HttpServletRequest req,
                         @Context HttpServletResponse response,
                         @RequestBody Map<String, String> json) throws IOException, WorkSpaceException {
        FileSystem fileSystem = null;
        InputStream inputStream = null;
        ServletOutputStream outputStream = null;
        try {
            String charset = json.get("charset");
            String userName = SecurityFilter.getLoginUsername(req);
            String path = json.get("path");
            if (StringUtils.isEmpty(path)) {
                throw new WorkSpaceException("Path(路径)：" + path + "Is empty!(为空！)");
            }
            WorkspaceUtil.pathSafeCheck(path,userName);
            if (StringUtils.isEmpty(charset)) {
                charset = "utf-8";
            }
            FsPath fsPath = new FsPath(path);
            //// TODO: 2018/11/29 Judging the directory, the directory cannot be downloaded(判断目录,目录不能下载)
            fileSystem = fsService.getFileSystem(userName, fsPath);
            fsValidate(fileSystem);
            if (!fileSystem.exists(fsPath)) {
                throw new WorkSpaceException("The downloaded directory does not exist!(下载的目录不存在!)");
            }
            inputStream = fileSystem.read(fsPath);
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            response.setCharacterEncoding(charset);
            java.nio.file.Path source = Paths.get(fsPath.getPath());
            response.addHeader("Content-Type", Files.probeContentType(source));
            response.addHeader("Content-Disposition", "attachment;filename="
                    + new File(fsPath.getPath()).getName());
            outputStream = response.getOutputStream();
            while ((bytesRead = inputStream.read(buffer, 0, 1024)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            response.reset();
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/plain; charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.append("error(错误):" + e.getMessage());
            writer.flush();
            writer.close();
        } finally {
            if (outputStream != null) {
                outputStream.flush();
            }
            StorageUtils.close(outputStream, inputStream, null);
        }
    }

    @GET
    @Path("/isExist")
    @Override
    public Response isExist(@Context HttpServletRequest req,
                            @QueryParam("path") String path) throws IOException, WorkSpaceException {
        String userName = SecurityFilter.getLoginUsername(req);
        FsPath fsPath = new FsPath(path);
        if (StringUtils.isEmpty(path)) {
            throw new WorkSpaceException("Path(路径)：" + path + "Is empty!(为空！)");
        }
        FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
        fsValidate(fileSystem);
        return Message.messageToResponse(Message.ok().data("isExist", fileSystem.exists(fsPath)));
    }

    /**
     * @param req
     * @param path
     * @param page
     * @param pageSize
     * @param charset
     * @return
     * @throws IOException
     */
    @GET
    @Path("/openFile")
    @Override
    public Response openFile(@Context HttpServletRequest req,
                             @QueryParam("path") String path,
                             @QueryParam("page") Integer page,
                             @QueryParam("pageSize") Integer pageSize,
                             @QueryParam("charset") String charset) throws IOException, WorkSpaceException {
        String userName = SecurityFilter.getLoginUsername(req);
        Message message = Message.ok();
        if (StringUtils.isEmpty(path)) {
            throw new WorkSpaceException("Path(路径)：" + path + "Is empty!(为空！)");
        }
        WorkspaceUtil.pathSafeCheck(path,userName);
        FsPath fsPath = new FsPath(path);
        FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
        fsValidate(fileSystem);
        if (StringUtils.isEmpty(page)) {
            page = 1;
        }
        //Throws an exception if the file does not have read access(如果文件没读权限，抛出异常)
        if (!fileSystem.canRead(fsPath)) {
            throw new WorkSpaceException("This user has no permission to read this file!");
        }
        Object fileContent = null;
        Integer totalLine = null;
        Integer totalPage = 0;
        String type = WorkspaceUtil.getOpenFileTypeByFileName(path);
        if ("script".equals(type)) {
            ScriptFsReader scriptFsReader = ScriptFsReader.getScriptFsReader(fsPath, StringUtils.isEmpty(charset) ? "utf-8" : charset, fileSystem.read(fsPath));
            MetaData metaData = scriptFsReader.getMetaData();
            ScriptMetaData scriptMetaData = (ScriptMetaData) metaData;
            Variable[] variables = scriptMetaData.getMetaData();
            StringBuilder stringBuilder = new StringBuilder();
            List<String> recordList = new ArrayList<>();
            Map<String, Object> params = VariableParser.getMap(variables);
            while (scriptFsReader.hasNext()) {
                ScriptRecord scriptRecord = (ScriptRecord) scriptFsReader.getRecord();
                recordList.add(scriptRecord.getLine());
            }
            if (pageSize == null || pageSize >= recordList.size()) {
                page = 1;
                totalPage = 1;
                for (int i = 0; i < recordList.size(); i++) {
                    String r = recordList.get(i);
                    if ("".equals(r)) {
                        stringBuilder.append("\n");
                    } else {
                        if (i != recordList.size() - 1) {
                            stringBuilder.append(r + "\n");
                        } else {
                            stringBuilder.append(r);
                        }
                    }
                }
            } else {
                if (recordList.size() % pageSize == 0) {
                    totalPage = recordList.size() / pageSize;
                } else {
                    totalPage = recordList.size() / pageSize + 1;
                }
                if (page >= totalPage) {
                    page = totalPage;
                    for (int i = (page - 1) * pageSize; i <= recordList.size() - 1; i++) {
                        String r = recordList.get(i);
                        if ("".equals(r)) {
                            stringBuilder.append("\n");
                        } else {
                            if (i != recordList.size() - 1) {
                                stringBuilder.append(r + "\n");
                            } else {
                                stringBuilder.append(r);
                            }
                        }
                    }
                } else {
                    for (int i = (page - 1) * pageSize; i <= page * pageSize - 1; i++) {
                        String r = recordList.get(i);
                        if ("".equals(r)) {
                            stringBuilder.append("\n");
                        } else {
                            if (i != page * pageSize - 1) {
                                stringBuilder.append(r + "\n");
                            } else {
                                stringBuilder.append(r);
                            }
                        }
                    }
                }
            }
            totalLine = recordList.size();
            fileContent = stringBuilder.toString();
            scriptFsReader.close();
            message.data("params", params);
            message.data("type", "script/text");
        } else if ("resultset".equals(type)) {
            //返回metadata
            ResultSetFactory instance = ResultSetFactory$.MODULE$.getInstance();
            ResultSet<? extends MetaData, ? extends Record> resultSet = instance.getResultSetByPath(fsPath);
            com.webank.wedatasphere.linkis.common.io.resultset.ResultSetReader<? extends MetaData, ? extends Record> resultSetReader = ResultSetReader.getResultSetReader(resultSet, fileSystem.read(fsPath));
            MetaData metaData = resultSetReader.getMetaData();
            if (StringUtils.isEmpty(pageSize)) {
                pageSize = 5000;
            }
            page = 1;
            Integer rowNum = 0;
            if (metaData instanceof LineMetaData) {
                LineMetaData lineMetaData = (LineMetaData) metaData;
                message.data("metadata", lineMetaData.getMetaData());
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < pageSize; i++) {
                    if (resultSetReader.hasNext()) {
                        rowNum++;
                        Record record = resultSetReader.getRecord();
                        LineRecord lineRecord = (LineRecord) record;
                        stringBuilder.append(lineRecord.getLine() + "\n");
                    } else {
                        break;
                    }
                }
                fileContent = stringBuilder.toString();
            } else if (metaData instanceof TableMetaData) {
                List<String> resultsetMetaDataList = new ArrayList<>();
                TableMetaData tableMetaData = (TableMetaData) metaData;
                Column[] columns = tableMetaData.columns();
                for (Column column : columns) {
                    resultsetMetaDataList.add(column.toString());
                }
                message.data("metadata", resultsetMetaDataList);
                List<ArrayList<String>> resultsetRow = new ArrayList<>();
                for (int i = 0; i < pageSize; i++) {
                    if (resultSetReader.hasNext()) {
                        rowNum++;
                        Record record = resultSetReader.getRecord();
                        TableRecord tableRecord = (TableRecord) record;
                        ArrayList<String> resulstsetColumn = new ArrayList<>();
                        Object[] row = tableRecord.row();
                        for (Object o : row) {
                            resulstsetColumn.add(o == null ? "NULL" : o.toString());
                        }
                        resultsetRow.add(resulstsetColumn);
                    } else {
                        break;
                    }
                }
                fileContent = resultsetRow;
            }
            totalLine = rowNum;
            resultSetReader.close();
            message.data("type", resultSet.resultSetType());
        }
        return Message.messageToResponse(message.data("page", page).data("totalLine", totalLine).data("totalPage", totalPage).data("fileContent", fileContent));
    }


    /**
     * @param req
     * @param json
     * @return
     * @throws IOException
     */
    @POST
    @Path("/saveScript")
    @Override
    public Response saveScript(@Context HttpServletRequest req, @RequestBody Map<String, Object> json) throws IOException, WorkSpaceException {
        String userName = SecurityFilter.getLoginUsername(req);
        String path = (String) json.get("path");
        if (StringUtils.isEmpty(path)) {
            throw new WorkSpaceException("Path(路径)：" + path + "is empty!(为空！)");
        }
        WorkspaceUtil.pathSafeCheck(path,userName);
        String charset = (String) json.get("charset");
        if (StringUtils.isEmpty(charset)) {
            charset = "utf-8";
        }
        String scriptContent = (String) json.get("scriptContent");
        Object params = json.get("params");
        Map<String, Object> map = (Map<String, Object>) params;
        Variable[] v = VariableParser.getVariables(map);
        FsPath fsPath = new FsPath(path);
        FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
        fsValidate(fileSystem);
        if (!fileSystem.exists(fsPath)) {
            throw new WorkSpaceException("file does not exist!(文件不存在！)");
        }
        if (!fileSystem.canWrite(fsPath)) {
            throw new WorkSpaceException("The user has no permission to modify the contents of this file and cannot save it!(该用户无权限对此文件内容进行修改，无法保存！)");
        }
        ScriptFsWriter scriptFsWriter = ScriptFsWriter.getScriptFsWriter(fsPath, charset, fileSystem.write(fsPath, true));
        scriptFsWriter.addMetaData(new ScriptMetaData(v));
        String[] split = scriptContent.split("\\n");
        for (int i = 0; i < split.length; i++) {
            if ("".equals(split[i])) {
                split[i] = "\n";
            } else {
                if (i != split.length - 1) {
                    split[i] += "\n";
                }
            }
            scriptFsWriter.addRecord(new ScriptRecord(split[i]));
        }
        scriptFsWriter.close();
        return Message.messageToResponse(Message.ok());
    }

    @GET
    @Path("resultsetToExcel")
    @Override
    public void resultsetToExcel(
            @Context HttpServletRequest req,
            @Context HttpServletResponse response,
            @QueryParam("path") String path,
            @QueryParam("charset") String charset,
            @QueryParam("outputFileType") String outputFileType,
            @QueryParam("outputFileName") String outputFileName) throws WorkSpaceException, IOException {
        InputStream inputStream = null;
        ByteArrayOutputStream os = null;
        ServletOutputStream outputStream = null;
        FileSystem fileSystem = null;
        CSVFsWriter csvfsWriter = null;
        ExcelFsWriter excelFsWriter = null;
        Integer index = 0;
        boolean isLimitDownloadSize = WorkSpaceConfiguration.RESULT_SET_DOWNLOAD_IS_LIMIT.getValue();
        com.webank.wedatasphere.linkis.common.io.resultset.ResultSetReader<? extends MetaData, ? extends Record> resultSetReader = null;
        try {
            if (StringUtils.isEmpty(charset)) {
                charset = "utf-8";
            }
            String userName = SecurityFilter.getLoginUsername(req);
            if (StringUtils.isEmpty(outputFileType)) {
                outputFileType = "csv";
            }
            if (StringUtils.isEmpty(outputFileName)) {
                outputFileName = "result";
            }
            if (StringUtils.isEmpty(path)) {
                throw new WorkSpaceException("Path(路径)：" + path + "is empty(为空)！");
            }
            WorkspaceUtil.pathSafeCheck(path,userName);
            String type = WorkspaceUtil.getOpenFileTypeByFileName(path);
            if (!"resultset".equals(type)) {
                throw new WorkSpaceException("unsupported type");
            }
            FsPath fsPath = new FsPath(path);
            fileSystem = fsService.getFileSystem(userName, fsPath);
            fsValidate(fileSystem);
            response.setCharacterEncoding(charset);
            ResultSetFactory instance = ResultSetFactory$.MODULE$.getInstance();
            ResultSet<? extends MetaData, ? extends Record> resultSet = instance.getResultSetByPath(fsPath);
            resultSetReader = ResultSetReader.getResultSetReader(resultSet, fileSystem.read(fsPath));
            MetaData metaData = resultSetReader.getMetaData();
            if ("csv".equals(outputFileType)) {
                response.addHeader("Content-Type", "text/plain");
                csvfsWriter = CSVFsWriter.getCSVFSWriter(charset, Constants.CSVDEFAULTSEPARATOR);
                if (metaData instanceof TableMetaData) {
                    TableMetaData tableMetaData = (TableMetaData) metaData;
                    csvfsWriter.addMetaData(tableMetaData);
                    while (resultSetReader.hasNext() && (!isLimitDownloadSize || index < WorkSpaceConfiguration.RESULT_SET_DOWNLOAD_MAX_SIZE_CSV.getValue())) {
                        index +=1;
                        csvfsWriter.addRecord(resultSetReader.getRecord());
                    }
                    inputStream = csvfsWriter.getCSVStream();
                } else {
                    StringBuilder stringBuilder = new StringBuilder();
                    LineMetaData lineMetaData = (LineMetaData) metaData;
                    if ("NULL".equals(lineMetaData)) {
                        stringBuilder.append(lineMetaData.getMetaData());
                        stringBuilder.append("\n");
                    }
                    while (resultSetReader.hasNext() && (!isLimitDownloadSize || index < WorkSpaceConfiguration.RESULT_SET_DOWNLOAD_MAX_SIZE_CSV.getValue())) {
                        index +=1;
                        LineRecord lineRecord = (LineRecord) resultSetReader.getRecord();
                        stringBuilder.append(lineRecord.getLine());
                        stringBuilder.append("\n");
                    }
                    inputStream = new ByteArrayInputStream(stringBuilder.toString().getBytes());
                }
            } else if ("xlsx".equals(outputFileType)) {
                if (!(metaData instanceof TableMetaData)) {
                    throw new WorkSpaceException("Only the result set of the table type can be converted to excel!(只有table类型的结果集能转为excel!)");
                }
                TableMetaData tableMetaData = (TableMetaData) metaData;
                response.addHeader("Content-Type", Constants.XLSXRESPONSE);
                excelFsWriter = ExcelFsWriter.getExcelFsWriter(Constants.FILEDEFAULTCHARSET, Constants.DEFAULTSHEETNAME, Constants.DEFAULTDATETYPE);
                excelFsWriter.addMetaData(tableMetaData);
                while (resultSetReader.hasNext() && (!isLimitDownloadSize || index < WorkSpaceConfiguration.RESULT_SET_DOWNLOAD_MAX_SIZE_EXCEL.getValue())) {
                    index +=1;
                    excelFsWriter.addRecord(resultSetReader.getRecord());
                }
                Workbook workBook = excelFsWriter.getWorkBook();
                os = new ByteArrayOutputStream();
                workBook.write(os);
                byte[] content = os.toByteArray();
                inputStream = new ByteArrayInputStream(content);
            } else {
                throw new WorkSpaceException("unsupported type");
            }
            response.addHeader("Content-Disposition", "attachment;filename="
                    + new String(outputFileName.getBytes("UTF-8"), "ISO8859-1") + "." + outputFileType);

            outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = inputStream.read(buffer, 0, 1024)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            LOGGER.error("output fail", e);
            response.reset();
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/plain; charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.append("error(错误):" + e.getMessage());
            writer.flush();
            writer.close();
        } finally {
            if (outputStream != null) {
                outputStream.flush();
            }
            StorageUtils.close(outputStream, inputStream, null);
            StorageUtils.close(os);
            if (csvfsWriter != null) {
                csvfsWriter.close();
            }
            if (excelFsWriter != null) {
                excelFsWriter.close();
            }
            if (resultSetReader != null) {
                resultSetReader.close();
            }
        }
    }

    @GET
    @Path("formate")
    @Override
    public Response formate(@Context HttpServletRequest req,
                            @QueryParam("path") String path,
                            @QueryParam("encoding") String encoding,
                            @QueryParam("fieldDelimiter") String fieldDelimiter,
                            @QueryParam("hasHeader") Boolean hasHeader,
                            @QueryParam("quote") String quote,
                            @QueryParam("escapeQuotes") Boolean escapeQuotes) throws Exception {
        String userName = SecurityFilter.getLoginUsername(req);
        if (StringUtils.isEmpty(path)) {
            throw new WorkSpaceException("Path(路径)：" + path + "is empty!(为空！)");
        }
        WorkspaceUtil.pathSafeCheck(path,userName);
        String suffix = path.substring(path.lastIndexOf("."));
        FsPath fsPath = new FsPath(path);
        Map<String, Object> res = new HashMap<String, Object>();
        FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
        fsValidate(fileSystem);
        InputStream in = fileSystem.read(fsPath);
        if (".xlsx".equalsIgnoreCase(suffix) || ".xls".equalsIgnoreCase(suffix)) {
            List<List<String>> info;
            info = ExcelStorageReader.getExcelTitle(in, null, hasHeader, suffix);
            res.put("columnName", info.get(1));
            res.put("columnType", info.get(2));
            res.put("sheetName", info.get(0));
        } else {
            if (StringUtils.isEmpty(encoding)) {
                encoding = Constants.FILEDEFAULTCHARSET;
            }
            if (StringUtils.isEmpty(fieldDelimiter)) {
                fieldDelimiter = Constants.CSVDEFAULTSEPARATOR;
            }
            if (StringUtils.isEmpty(quote)) {
                quote = "\"";
            }
            if (hasHeader == null) {
                hasHeader = false;
            }
            if (escapeQuotes == null) {
                escapeQuotes = false;
            }
            String[][] column = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));
            String header = reader.readLine();
            if (StringUtils.isEmpty(header)) {
                throw new WorkSpaceException("The file content is empty and cannot be imported!(文件内容为空，不能进行导入操作！)");
            }
            String[] line = header.split(fieldDelimiter);
            int colNum = line.length;
            column = new String[2][colNum];
            if (hasHeader) {
                for (int i = 0; i < colNum; i++) {
                    column[0][i] = line[i];
                    if (escapeQuotes) {
                        try {
                            column[0][i] = column[0][i].substring(1, column[0][i].length() - 1);
                        } catch (StringIndexOutOfBoundsException e) {
                            throw new WorkSpaceException("The header of the file has no qualifiers. Do not check the first behavior header or set no qualifier!(该文件的表头没有限定符，请勿勾选首行为表头或者设置无限定符！)");
                        }
                    }
                    column[1][i] = "string";
                }
            } else {
                for (int i = 0; i < colNum; i++) {
                    column[0][i] = "col_" + (i + 1);
                    column[1][i] = "string";
                }
            }
            res.put("columnName", column[0]);
            res.put("columnType", column[1]);
        }
        StorageUtils.close(null, in, null);
        return Message.messageToResponse(Message.ok().data("formate", res));
    }

    @GET
    @Path("/openLog")
    @Override
    public Response openLog(@Context HttpServletRequest req, @QueryParam("path") String path) throws IOException, WorkSpaceException {
        String userName = SecurityFilter.getLoginUsername(req);
        if (StringUtils.isEmpty(path)) {
            throw new WorkSpaceException("Path(路径)：" + path + "is empty!(为空！)");
        }
        WorkspaceUtil.pathSafeCheck(path,userName);
        FsPath fsPath = new FsPath(path);
        FileSystem fileSystem = fsService.getFileSystem(userName, fsPath);
        fsValidate(fileSystem);
        if (!fileSystem.canRead(fsPath)) {
            throw new WorkSpaceException("This user has no permission to read this log!(该用户无权限读取此日志！)");
        }
        String type = WorkspaceUtil.getOpenFileTypeByFileName(path);
        if (!"script".equals(type)) {
            throw new WorkSpaceException("This file is not a log file!(该文件不是日志文件！)");
        }
        ScriptFsReader scriptFsReader = ScriptFsReader.getScriptFsReader(fsPath, "utf-8", fileSystem.read(fsPath));
        MetaData metaData = scriptFsReader.getMetaData();
        StringBuilder info = new StringBuilder();
        StringBuilder warn = new StringBuilder();
        StringBuilder error = new StringBuilder();
        StringBuilder all = new StringBuilder();
        StringBuilder tmp = null;
        while (scriptFsReader.hasNext()) {
            ScriptRecord scriptRecord = (ScriptRecord) scriptFsReader.getRecord();
            String line = scriptRecord.getLine();
            all.append(line + "\n");
            if (WorkspaceUtil.logMatch(line, WorkspaceUtil.infoReg)) {
                info.append(line + "\n");
                //tmp = info;
            } else if (WorkspaceUtil.logMatch(line, WorkspaceUtil.errorReg)) {
                error.append(line + "\n");
                //tmp = error;
            } else if (WorkspaceUtil.logMatch(line, WorkspaceUtil.warnReg)) {
                warn.append(line + "\n");
                //tmp = warn;
            }/*else {
                    if (tmp !=null){
                        tmp.append(line + "\n");
                    }
                }*/
        }
        scriptFsReader.close();
        ArrayList<String> log = new ArrayList<>();
        log.add(error.toString());
        log.add(warn.toString());
        log.add(info.toString());
        log.add(all.toString());
        return Message.messageToResponse(Message.ok().data("log", log));
    }

    private static void deleteAllFiles(FileSystem fileSystem, FsPath fsPath) throws IOException {
        fileSystem.delete(fsPath);
        List<FsPath> list = null;
        if (fileSystem.exists(fsPath)) {
            list = fileSystem.list(fsPath);
        }
        if (list == null) {
            return;
        }
        for (FsPath path : list) {
            deleteAllFiles(fileSystem, path);
        }
        fileSystem.delete(fsPath);
    }
}
