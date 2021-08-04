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

package com.webank.wedatasphere.linkis.entrance.parser;

import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration$;
import com.webank.wedatasphere.linkis.governance.common.entity.job.JobRequest;
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtil;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class ParserUtils {

    private static final Map<String, String> types = new HashMap<>();

    static{
        types.put("py", "python");
        types.put("python", "python");
        types.put("sql", "sql");
        types.put("pyspark", "python");
        types.put("scala", "scala");
        types.put("rspark", "r");
        types.put("r", "r");
        types.put("java", "java");
        types.put("hql", "hql");
        types.put("sparksql", "sql");
    }

    public static void generateLogPath(JobRequest jobRequest, Map<String, String> responseQueryConfig){
        String logPath = null;
        String logPathPrefix = null;
        String logMid = "log";
        if (responseQueryConfig != null){
            logPathPrefix = responseQueryConfig.get(EntranceConfiguration$.MODULE$.CLOUD_CONSOLE_LOGPATH_KEY().getValue());
        }
        if (StringUtils.isEmpty(logPathPrefix)){
            logPathPrefix = EntranceConfiguration$.MODULE$.DEFAULT_LOGPATH_PREFIX().getValue();
            String umUser = jobRequest.getSubmitUser();
            if (logPathPrefix.endsWith("/")){
                logPathPrefix = logPathPrefix + umUser;
            }else{
                logPathPrefix = logPathPrefix + "/" + umUser;
            }
        }
        /*Determine whether logPathPrefix is terminated with /, if it is, delete */
        /*判断是否logPathPrefix是否是以 / 结尾， 如果是，就删除*/
        if(logPathPrefix.endsWith("/")){
            logPathPrefix = logPathPrefix.substring(0, logPathPrefix.length() - 1);
        }
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd");
        String dateString = dateFormat.format(date);
        String creator = LabelUtil.getUserCreator(jobRequest.getLabels())._2;
        logPath = logPathPrefix + "/" + "log" +
                "/" + creator + "/" + dateString + "/" + jobRequest.getId() + ".log";
        jobRequest.setLogPath(logPath);
    }

    public static String getCorrespondingType(String runType){
        return types.get(runType.toLowerCase());
    }

}
