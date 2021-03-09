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

/**
 * author: enjoyyin
 * date: 2018/9/27
 * time: 10:26
 * Description:
 */
package com.webank.wedatasphere.linkis.entrance.persistence;

import com.google.gson.Gson;
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration;
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration$;
import com.webank.wedatasphere.linkis.entrance.exception.EntranceIllegalParamException;
import com.webank.wedatasphere.linkis.entrance.exception.EntranceRPCException;
import com.webank.wedatasphere.linkis.entrance.exception.QueryFailedException;
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant;
import com.webank.wedatasphere.linkis.protocol.query.*;
import com.webank.wedatasphere.linkis.protocol.task.Task;
import com.webank.wedatasphere.linkis.rpc.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryPersistenceEngine extends AbstractPersistenceEngine{

    private Sender sender;

    private static final Logger logger = LoggerFactory.getLogger(QueryPersistenceEngine.class);

    public QueryPersistenceEngine(){
        /*
            Get the corresponding sender through datawork-cloud-publicservice(通过datawork-cloud-publicservice 拿到对应的sender)
         */
         sender = Sender.getSender(EntranceConfiguration$.MODULE$.QUERY_PERSISTENCE_SPRING_APPLICATION_NAME().getValue());
    }



    @Override
    public void persist(Task task) throws QueryFailedException, EntranceIllegalParamException, EntranceRPCException{
        if (task == null){
            throw new EntranceIllegalParamException(20004, "task can not be null, unable to do persist operation");
        }
        RequestInsertTask requestInsertTask = new RequestInsertTask();
        if(task instanceof RequestPersistTask){
            RequestPersistTask requestPersistTask = (RequestPersistTask)task;
            BeanUtils.copyProperties(requestPersistTask, requestInsertTask);
            ResponsePersist responsePersist = null;
            try{
                responsePersist = (ResponsePersist) sender.ask(requestInsertTask);
            }catch(Exception e){
                throw new EntranceRPCException(20020, "sender rpc failed", e);
            }

            if (responsePersist != null){
                int status = responsePersist.getStatus();
                String message = responsePersist.getMsg();
                if (status != 0 ){
                    throw new QueryFailedException(20011, "insert task failed, reason: " + message);
                }
                Map<String, Object> data = responsePersist.getData();
                Object object = data.get(TaskConstant.TASKID);
                if (object == null){
                    throw new QueryFailedException(20011, "insert task failed, reason: " + message);
                }
                String taskStr = object.toString();
                Long taskID = Long.parseLong(taskStr.substring(0,taskStr.indexOf(".")));
                ((RequestPersistTask) task).setTaskID(taskID);
            }
        }else{
            //Todo can be throw exception if it is not requestPersistTask(todo 如果不是 requestPersistTask的话，可以进行throw异常)
        }
    }
    @Override
    public Task retrieve(Long taskID)throws EntranceIllegalParamException, QueryFailedException, EntranceRPCException{

        Task task = null;

        if ( taskID == null ||  taskID < 0){
            throw new EntranceIllegalParamException(20003, "taskID can't be null or less than 0");
        }

        RequestQueryTask requestQueryTask = new RequestQueryTask();
        requestQueryTask.setTaskID(taskID);
        ResponsePersist responsePersist = null;
        try {
            responsePersist = (ResponsePersist) sender.ask(requestQueryTask);
        }catch(Exception e){
            logger.error("Requesting the corresponding task failed with taskID: {}(通过taskID: {} 请求相应的task失败)", taskID, e);
            throw new EntranceRPCException(20020, "sender rpc failed", e);
        }
        int status = responsePersist.getStatus();
        //todo I want to discuss it again.(要再讨论下)
        String message = responsePersist.getMsg();
        if (status != 0){
            logger.error("By taskID: {} request the corresponding task return status code is not 0, the query fails(通过taskID: {} 请求相应的task返回状态码不为0，查询失败)", taskID);
            throw new QueryFailedException(20010, "retrieve task failed, reason: " + message);
        }
        java.util.Map<String, Object> data = responsePersist.getData();
        if (data != null){
           Object object = data.get(TaskConstant.TASK);
           if (object instanceof List){
               List list = (List)object;
               if (list.size() == 0){
                   logger.info("returned list length is 0, maybe there is no task corresponding to {}", taskID);
               }else if (list.size() == 1){
                   Object t = list.get(0);
                   Gson gson = new Gson();
                   String json = gson.toJson(t);
                   task = gson.fromJson(json, RequestPersistTask.class);
               }
           }
        }
        return task;
    }
    @Override
    public void updateIfNeeded(Task task)throws EntranceRPCException, EntranceIllegalParamException{
        if (task == null){
            throw new EntranceIllegalParamException(20004, "task can not be null, unable to do update operation");
        }
        if(task instanceof RequestPersistTask){
            RequestUpdateTask requestUpdateTask = new RequestUpdateTask();
            RequestPersistTask requestPersistTask = (RequestPersistTask)task;
            BeanUtils.copyProperties(requestPersistTask, requestUpdateTask);
            try{
                sender.ask(requestUpdateTask);
            }catch(Exception e){
                logger.error("Request to update task with taskID {} failed, possibly due to RPC failure(请求更新taskID为 {} 的任务失败，原因可能是RPC失败)", requestUpdateTask.getTaskID(), e);
                throw new EntranceRPCException(20020, "sender rpc failed ", e);
            }
        }
    }
    @Override
    public Task[] readAll(String instance)throws EntranceIllegalParamException, EntranceRPCException,QueryFailedException{

        List<Task> retList = new ArrayList<>();

        if (instance == null || "".equals(instance)){
            throw new EntranceIllegalParamException(20004, "instance can not be null");
        }

        RequestReadAllTask requestReadAllTask = new RequestReadAllTask(instance);
        ResponsePersist responsePersist = null;
        try{
            responsePersist = (ResponsePersist)sender.ask(requestReadAllTask);
        }catch(Exception e){
            throw new EntranceRPCException(20020, "sender rpc failed ", e);
        }
        if (responsePersist != null){
            int status = responsePersist.getStatus();
            String message = responsePersist.getMsg();
            if (status != 0){
                throw new QueryFailedException(20011, "read all tasks failed, reason: " + message);
            }
            Map<String, Object> data = responsePersist.getData();
            Object object = data.get(TaskConstant.TASK);
            if( object instanceof List){
                List list = (List)object;
                if (list.size() == 0){
                    logger.info("no running task in this instance: {}", instance);
                }
                for(Object o : list){
                    if (o instanceof RequestPersistTask ){
                        retList.add((RequestPersistTask)o);
                    }
                }
            }
        }
        return retList.toArray(new Task[0]);
    }
    @Override
    public void close() throws IOException {

    }

    @Override
    public void flush() throws IOException {

    }
}
