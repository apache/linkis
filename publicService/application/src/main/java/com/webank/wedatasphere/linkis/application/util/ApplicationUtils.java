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

package com.webank.wedatasphere.linkis.application.util;

import com.webank.wedatasphere.linkis.application.conf.ApplicationScalaConfiguration;
import com.webank.wedatasphere.linkis.application.exception.ApplicationException;
import com.webank.wedatasphere.linkis.common.io.FsPath;
import com.webank.wedatasphere.linkis.common.utils.Utils;
import com.webank.wedatasphere.linkis.storage.FSFactory;
import com.webank.wedatasphere.linkis.storage.fs.FileSystem;
import com.webank.wedatasphere.linkis.application.conf.ApplicationConfiguration;
import com.webank.wedatasphere.linkis.storage.utils.FileSystemUtils;
import lombok.Cleanup;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Created by johnnwang on 2019/1/18.
 */
public class ApplicationUtils {

    public static String writeJsonToHDFS(String creator,String flows,Boolean isInit) throws IOException {
        String jsonPath;
        if(!isInit){
            jsonPath = ApplicationConfiguration.JSON_ROOT_PATH.getValue().toString()+creator+"/application/"+ "application_"+creator+"_"+UUID.randomUUID().toString().replaceAll("-","").substring(1,8)+".wf";
        }else {
            jsonPath = ApplicationConfiguration.JSON_ROOT_PATH.getValue().toString()+creator+"/application/"+ "application_"+creator+"_init.wf";
        }
        FsPath fsPath = new FsPath(jsonPath);
        /*@Cleanup FileSystem fs = (FileSystem) FSFactory.getFs(fsPath);
        fs.init(null);
        fs.createNewFile(fsPath);*/
        FileSystemUtils.createNewFile(fsPath,creator,true);
        @Cleanup FileSystem fs = (FileSystem) FSFactory.getFs(fsPath);
        fs.init(null);
        @Cleanup OutputStream os = fs.write(fsPath, true);
        @Cleanup ByteArrayInputStream is = new ByteArrayInputStream(flows.getBytes());
        IOUtils.copy(is,os);
        fs.setOwner(fsPath,creator,creator);
        return jsonPath;
    }


    public static void updateFile(String creator,String flows,String path) throws IOException, ApplicationException {
        FsPath fsPath = new FsPath(path);
        @Cleanup FileSystem fs = (FileSystem) FSFactory.getFs(fsPath);
        fs.init(null);
        if (!fs.exists(fsPath)){
            throw new ApplicationException("The file to save the app does not exist!(保存应用的文件不存在！)");
        }
        @Cleanup OutputStream os = fs.write(fsPath, true);
        @Cleanup ByteArrayInputStream is = new ByteArrayInputStream(flows.getBytes());
        IOUtils.copy(is,os);
        fs.setOwner(fsPath,creator,creator);
    }


/*    public static Boolean fileIsEmpty(String jsonPath) throws IOException {
        FsPath fsPath = new FsPath(jsonPath);
        FileSystem fs = (FileSystem) FSFactory.getFs(fsPath);
        return fs.read(fsPath).available() == 0;
    }*/

    public static void removeOldJson(String jsonPath) throws IOException {
        FsPath fsPath = new FsPath(jsonPath);
        @Cleanup FileSystem fs = (FileSystem) FSFactory.getFs(fsPath);
        fs.init(null);
        if (fs.exists(fsPath)){
            fs.delete(fsPath);
        }
    }

    public static void createDir(String creator) throws IOException {
        FsPath fsPath = new FsPath("hdfs:///tmp/dwc/application/");
        @Cleanup FileSystem fs = (FileSystem) FSFactory.getFs(fsPath);
        fs.init(null);
        fs.mkdir(new FsPath("hdfs:///tmp/dwc/application/"+creator));
    }

    public static void createExampleFile(String creator) throws IOException, NoSuchFieldException, IllegalAccessException {
       /* createExampleFile(ApplicationScalaConfiguration.INIT_EXAMPLE_SQL_NAME().getValue(),creator,"show databases;");
        createExampleFile(ApplicationScalaConfiguration.INIT_EXAMPLE_SCALA_NAME().getValue(),creator,"print(\"hello dwc\")");
        createExampleFile(ApplicationScalaConfiguration.INIT_EXAMPLE_SPY_NAME().getValue(),creator,"print(\"hello dwc\")");
        createExampleFile(ApplicationScalaConfiguration.INIT_EXAMPLE_HQL_NAME().getValue(),creator,"show databases;");
        createExampleFile(ApplicationScalaConfiguration.INIT_EXAMPLE_PYTHON_NAME().getValue(),creator,"print(\"hello dwc\")");*/
    }

    public static  void createExampleFile(String fileName,String creator,String fileContent) throws IOException, NoSuchFieldException, IllegalAccessException {
        String jsonPath = ApplicationScalaConfiguration.INIT_EXAMPLE_PATH().getValue().toString()+creator+"/application/dataStudio/"+ fileName;
        FsPath fsPath = new FsPath(jsonPath);
        FileSystemUtils.createNewFile(fsPath,creator,true);
        @Cleanup FileSystem fs = (FileSystem) FSFactory.getFs(fsPath);
        fs.init(null);
        @Cleanup OutputStream os = fs.write(fsPath, true);
        @Cleanup ByteArrayInputStream is = new ByteArrayInputStream(fileContent.getBytes());
        IOUtils.copy(is,os);
        fs.setOwner(fsPath,creator,creator);
    }

    public static String writeJsonToHDFSNew(String jsonPath,String creator,String flows) throws IOException {
        FsPath fsPath = new FsPath(jsonPath);
        FileSystemUtils.createNewFile(fsPath,creator,true);
        @Cleanup FileSystem fs = (FileSystem) FSFactory.getFs(fsPath);
        fs.init(null);
        @Cleanup OutputStream os = fs.write(fsPath, true);
        @Cleanup ByteArrayInputStream is = new ByteArrayInputStream(flows.getBytes());
        IOUtils.copy(is,os);
        fs.setOwner(fsPath,creator,creator);
        return jsonPath;
    }
}
