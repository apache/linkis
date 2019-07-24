//package com.webank.wedatasphere.linkis.entrance.parser;
//
//import com.webank.wedatasphere.linkis.common.exception.ErrorException;
//import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration$;
//import com.webank.wedatasphere.linkis.entrance.exception.EntranceIllegalParamException;
//import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant;
//import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask;
//import com.webank.wedatasphere.linkis.protocol.task.Task;
//import com.webank.wedatasphere.linkis.server.rpc.Sender;
//import org.apache.commons.lang.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Date;
//import java.util.Map;
///**
// * @author: enjoyyin
// * date: 2018/10/8
// * time: 9:24
// * Description:
// */
//
//public class CommonEntranceParser extends AbstractEntranceParser {
//
//    private static final Logger logger = LoggerFactory.getLogger(CommonEntranceParser.class);
//    /**
//     * @param params
//     * @return
//     */
//    @Override
//    public Task parseToTask(Map<String, Object> params) throws ErrorException{
//        RequestPersistTask task = new RequestPersistTask();
//        task.setCreatedTime(new Date(System.currentTimeMillis()));
//        task.setInstance(Sender.getThisInstance());
//        String umUser = (String) params.get(TaskConstant.UMUSER);
//        if (umUser == null){
//            throw new EntranceIllegalParamException(20005, "umUser can not be null");
//        }
//        task.setUmUser(umUser);
//        String executionCode = (String) params.get(TaskConstant.EXECUTIONCODE);
//        Object args = params.get(TaskConstant.ARGS);
//        if (args instanceof Map){
//            task.setArgs((Map)args);
//        }
//        Boolean formatCode = (Boolean) params.get(TaskConstant.FORMATCODE);
//        String creator = (String) params.get(TaskConstant.REQUESTAPPLICATIONNAME);
//        String scriptPath = (String) params.get(TaskConstant.SCRIPTPATH);
//        String executeApplicationName = (String) params.get(TaskConstant.EXECUTEAPPLICATIONNAME);
//        if(StringUtils.isEmpty(creator)){
//            /*默认为IDE*/
//            creator = EntranceConfiguration$.MODULE$.DEFAULT_REQUEST_APPLICATION_NAME().getValue();
//        }
//        if (StringUtils.isEmpty(executeApplicationName)){
//            throw new EntranceIllegalParamException(20006,"param executeApplicationName can not be empty or null");
//        }
//        /*当执行类型为IDE的时候，executionCode和scriptPath不能同时为空*/
//        if (EntranceConfiguration$.MODULE$.DEFAULT_REQUEST_APPLICATION_NAME().getValue().equals(creator)&&
//                    StringUtils.isEmpty(scriptPath) && StringUtils.isEmpty(executionCode)){
//            throw new EntranceIllegalParamException(20007, "param executionCode and scriptPath can not be empty at the same time");
//        }
//        if (formatCode == null){
//            formatCode = false;
//        }
//        String runType = null;
//        if(StringUtils.isNotEmpty(executionCode)){
//            runType = (String) params.get(TaskConstant.RUNTYPE);
//            if (StringUtils.isEmpty(runType)){
//                runType = EntranceConfiguration$.MODULE$.DEFAULT_RUN_TYPE().getValue();
//            }
//            //如果formatCode 不为空的话，我们需要将其进行格式化
//            if (formatCode){
//                executionCode = formatCode(executionCode);
//            }
//            task.setExecutionCode(executionCode);
//        }
//        if (StringUtils.isNotEmpty(scriptPath)){
//            String[] strings = StringUtils.split(scriptPath, ".");
//            if (strings.length >= 2){
//                /*获得执行脚本文件的后缀名,如果不能从一个执行脚本的后缀名判断出类型，可能需要报错*/
//                //todo 如果不能从文件后缀名得到
//                runType = strings[strings.length - 1];
//                if (ParserUtils.getCorrespondingType(runType) != null){
//                    runType = ParserUtils.getCorrespondingType(runType);
//                }else{
//                    logger.warn("未能找到相应的执行类型:" + runType );
//                }
//            }else{
//                logger.warn("scriptPath:" + scriptPath + "没有后缀名, 无法判断任务是哪种类型");
//            }
//        }
//        task.setEngineType(runType);
//        //为了兼容代码，让engineType和runType都有同一个属性
//        task.setRunType(runType);
//        task.setExecuteApplicationName(executeApplicationName);
//        task.setRequestApplicationName(creator);
////        String configurationName = EntranceConfiguration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME().getValue();
////        Sender sender = Sender.getSender(configurationName);
////        RequestQueryGolbalConfig requestQueryGolbalConfig = new RequestQueryGolbalConfig(umUser);
////        /*我们需要将组装好的task传入到query模块进行数据库的存储，得到唯一的taskID*/
////        getEntranceContext().getOrCreatePersistenceManager().createPersistenceEngine().persist(task);
////        ResponseQueryConfig responseQueryConfig = null;
////        String logPath = null;
////        try{
////            responseQueryConfig = (ResponseQueryConfig) sender.ask(requestQueryGolbalConfig);
////        }catch(Exception e){
////            QueryFailedException queryFailed = new QueryFailedException(20021, "获取用户设置的日志路径失败！原因：RPC请求" + configurationName + "服务失败！");
////            queryFailed.initCause(e);
////            throw queryFailed;
////        }
////        if (responseQueryConfig != null){
////            Map<String, String> keyAndValue = responseQueryConfig.getKeyAndValue();
////            logPath = keyAndValue.get(EntranceConfiguration.CLOUD_CONSOLE_LOGPATH_KEY().value());
////        }
////        if (logPath == null){
////            logPath = ParserUtils.generateLogPath(task, responseQueryConfig);
////        }
////        task.setLogPath(logPath);
//        return task;
//    }
//
//    private String formatCode(String executionCode) {
//        //todo 格式化代码
//        return executionCode;
//    }
//
//
//}
