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

package com.webank.wedatasphere.linkis.filesystem.util;

import com.webank.wedatasphere.linkis.filesystem.conf.WorkSpaceConfiguration;
import com.webank.wedatasphere.linkis.filesystem.exception.WorkSpaceException;
import com.webank.wedatasphere.linkis.storage.utils.StorageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Created by johnnwang on 2018/11/5.
 */
public class WorkspaceUtil {
    private static String[] namenodes;
    private static String linuxUserManagerParentPath;

    public static String infoReg = "((19|20)[0-9]{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01]) "
            + "([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]" +"\\.\\d{3}\\s*INFO(.*)";
    public static String warnReg = "((19|20)[0-9]{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01]) "
            + "([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]" +"\\.\\d{3}\\s*WARN(.*)";
    public static String errorReg = "((19|20)[0-9]{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01]) "
            + "([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]" +"\\.\\d{3}\\s*ERROR(.*)";

    public static String getOpenFileTypeByFileName(String path) throws WorkSpaceException {
        if (StringUtils.isEmpty(path)) {
        }
        if (path.endsWith(".sql")
                || path.endsWith(".hql")
                || path.endsWith(".txt")
                || path.endsWith(".python")
                || path.endsWith(".log")
                || path.endsWith(".r")
                || path.endsWith(".out")
                || path.endsWith(".scala")
                || path.endsWith(".py")
                || path.endsWith(".mlsql")
        ) {
            return "script";
        } else if (path.endsWith(".dolphin")) {
            return "resultset";
        } else {
            throw new WorkSpaceException("unsupported type!");
        }
    }

    public static Boolean logMatch(String code ,String pattern){
        return Pattern.matches(pattern,code);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceUtil.class);

    //TODO update pathSafeCheck rule
    public static void pathSafeCheck(String path,String userName) throws WorkSpaceException {
        /*LOGGER.info("start safe check path params..");
        LOGGER.info(path);
        String userLocalRootPath = null;
        if (WorkSpaceConfiguration.LOCAL_USER_ROOT_PATH.getValue().toString().endsWith(File.separator)){
            userLocalRootPath = WorkSpaceConfiguration.LOCAL_USER_ROOT_PATH.getValue() + userName;
        }else{
            userLocalRootPath = WorkSpaceConfiguration.LOCAL_USER_ROOT_PATH.getValue() + File.separator + userName;
        }
        String userHdfsRootPath = null;
        if (WorkSpaceConfiguration.HDFS_USER_ROOT_PATH_PREFIX.getValue().toString().endsWith(File.separator)){
            userHdfsRootPath = WorkSpaceConfiguration.HDFS_USER_ROOT_PATH_PREFIX.getValue() + userName + WorkSpaceConfiguration.HDFS_USER_ROOT_PATH_SUFFIX.getValue();
        }else{
            userHdfsRootPath = WorkSpaceConfiguration.HDFS_USER_ROOT_PATH_PREFIX.getValue() + File.separator + userName + WorkSpaceConfiguration.HDFS_USER_ROOT_PATH_SUFFIX.getValue();
        }
        LOGGER.info(userLocalRootPath);
        LOGGER.info(userHdfsRootPath);
        if(!path.contains(StorageUtils.FILE_SCHEMA()) && !path.contains(StorageUtils.HDFS_SCHEMA())){
            throw new WorkSpaceException("the path should contain schema");
        }
        if(path.contains("../")){
            throw new WorkSpaceException("Relative path not allowed");
        }
        if(!path.contains(userLocalRootPath) && !path.contains(userHdfsRootPath)){
            throw new WorkSpaceException("The path needs to be within the user's own workspace path");
        }*/
    }

    public static void fileAndDirNameSpecialCharCheck(String path) throws WorkSpaceException {
        String name = new File(path).getName();
        LOGGER.info(path);
        String specialRegEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern specialPattern = Pattern.compile(specialRegEx);
        if(specialPattern.matcher(name).find()){
            throw new WorkSpaceException("the path exist special char");
        }
    }

}
