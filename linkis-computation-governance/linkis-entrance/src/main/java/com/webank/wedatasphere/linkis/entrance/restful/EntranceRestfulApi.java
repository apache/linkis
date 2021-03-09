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

package com.webank.wedatasphere.linkis.entrance.restful;

import com.webank.wedatasphere.linkis.common.log.LogUtils;
import com.webank.wedatasphere.linkis.entrance.EntranceServer;
import com.webank.wedatasphere.linkis.entrance.annotation.EntranceServerBeanAnnotation;
import com.webank.wedatasphere.linkis.entrance.background.BackGroundService;
import com.webank.wedatasphere.linkis.entrance.execute.EntranceJob;
import com.webank.wedatasphere.linkis.entrance.log.LogReader;
import com.webank.wedatasphere.linkis.entrance.utils.JobHistoryHelper;
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant;
import com.webank.wedatasphere.linkis.protocol.engine.JobProgressInfo;
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask;
import com.webank.wedatasphere.linkis.protocol.task.Task;
import com.webank.wedatasphere.linkis.protocol.utils.ZuulEntranceUtils;
import com.webank.wedatasphere.linkis.rpc.Sender;
import com.webank.wedatasphere.linkis.scheduler.queue.Job;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;
import com.webank.wedatasphere.linkis.server.socket.controller.ServerEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import scala.Option;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * created by enjoyyin on 2018/10/16
 * Description: an implementation class of EntranceRestfulRemote
 */
@Path("/entrance")
@Component
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EntranceRestfulApi implements EntranceRestfulRemote {

    private EntranceServer entranceServer;

    private static final Logger logger = LoggerFactory.getLogger(EntranceRestfulApi.class);
    @EntranceServerBeanAnnotation.EntranceServerAutowiredAnnotation
    public void setEntranceServer(EntranceServer entranceServer){
        this.entranceServer = entranceServer;
    }

    /**
     * The execute function handles the request submitted by the user to execute the task, and the execution ID is returned to the user.
     * execute函数处理的是用户提交执行任务的请求，返回给用户的是执行ID
     * json Incoming key-value pair(传入的键值对)
     * Repsonse
     */
    @Override
    @POST
    @Path("/execute")
    public Response execute(@Context HttpServletRequest req, Map<String, Object> json) {
        Message message = null;
//        try{
            logger.info("Begin to get an execID");
            json.put(TaskConstant.UMUSER, SecurityFilter.getLoginUsername(req));
            String execID = entranceServer.execute(json);
            Job job = entranceServer.getJob(execID).get();
            Task task = ((EntranceJob)job).getTask();
            Long taskID = ((RequestPersistTask)task).getTaskID();
            pushLog(LogUtils.generateInfo("You have submitted a new job, script code (after variable substitution) is"), job);
            pushLog("************************************SCRIPT CODE************************************", job);
            pushLog(((RequestPersistTask)task).getCode(), job);
            pushLog("************************************SCRIPT CODE************************************", job);
            pushLog(LogUtils.generateInfo("Your job is accepted,  jobID is " + execID + " and taskID is " + taskID + ". Please wait it to be scheduled"), job);
            execID = ZuulEntranceUtils.generateExecID(execID, ((RequestPersistTask) task).getExecuteApplicationName(),
                    Sender.getThisInstance(), ((RequestPersistTask)task).getRequestApplicationName());
            message = Message.ok();
            message.setMethod("/api/entrance/execute");
            message.data("execID",execID);
            message.data("taskID", taskID);
            logger.info("End to get an an execID: {}, taskID: {}", execID, taskID);
//        }catch(ErrorException e){
//            message = Message.error(e.getDesc());
//            message.setStatus(1);
//            message.setMethod("/api/entrance/execute");
//        }
        return Message.messageToResponse(message);

    }

    private void pushLog(String log, Job job){
        entranceServer.getEntranceContext().getOrCreateLogManager().onLogUpdate(job, log);
    }

    @Override
    @GET
    @Path("/{id}/status")
    public Response status(@PathParam("id") String id, @QueryParam("taskID")String taskID) {
        Message message = null;
        String realId = ZuulEntranceUtils.parseExecID(id)[3];
        Option<Job> job = Option.apply(null);
        try{
            job = entranceServer.getJob(realId);
        }catch(Exception e){
            logger.warn("获取任务 {} 状态时出现错误", realId, e);
            //如果获取错误了,证明在内存中已经没有了,去jobhistory找寻一下taskID代表的任务的状态，然后返回
            long realTaskID = Long.parseLong(taskID);
            String status = JobHistoryHelper.getStatusByTaskID(realTaskID);
            message = Message.ok();
            message.setMethod("/api/entrance/" + id + "/status");
            message.data("status",status).data("execID", id);
            return Message.messageToResponse(message);
        }
        if (job.isDefined()){
            message = Message.ok();
            message.setMethod("/api/entrance/" + id + "/status");
            message.data("status", job.get().getState().toString()).data("execID", id);
        }else{
            message = Message.error("ID The corresponding job is empty and cannot obtain the corresponding task status.(ID 对应的job为空，不能获取相应的任务状态)");
        }
        return Message.messageToResponse(message);
    }




    @Override
    @GET
    @Path("/{id}/progress")
    public Response progress(@PathParam("id")String id) {
        Message message = null;
        String realId = ZuulEntranceUtils.parseExecID(id)[3];
        Option<Job> job = entranceServer.getJob(realId);
        if (job.isDefined()){
            JobProgressInfo[] jobProgressInfos = ((EntranceJob)job.get()).getProgressInfo();
            if (jobProgressInfos == null){
                message = Message.error("Can not get the corresponding progress information, it may be that the corresponding progress information has not been generated(不能获取相应的进度信息,可能是相应的进度信息还未生成)");
                message.setMethod("/api/entrance/" + id + "/progress");
            }else{
                List<Map<String, Object>> list = new ArrayList<>();
                for(JobProgressInfo jobProgressInfo : jobProgressInfos) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", jobProgressInfo.id());
                    map.put("succeedTasks", jobProgressInfo.succeedTasks());
                    map.put("failedTasks", jobProgressInfo.failedTasks());
                    map.put("runningTasks", jobProgressInfo.runningTasks());
                    map.put("totalTasks", jobProgressInfo.totalTasks());
                    list.add(map);
                }
                message = Message.ok();
                message.setMethod("/api/entrance/" + id + "/progress");
                message.data("progress",job.get().getProgress()).data("execID", id).data("progressInfo", list);
            }
        }else{
            message = Message.error("The job corresponding to the ID is empty, and the corresponding task progress cannot be obtained.(ID 对应的job为空，不能获取相应的任务进度)");
        }
        return Message.messageToResponse(message);
    }

    @Override
    @GET
    @Path("/{id}/log")
    public Response log(@Context HttpServletRequest req, @PathParam("id")String id) {
        String realId = ZuulEntranceUtils.parseExecID(id)[3];
        Option<Job> job = Option.apply(null);
        Message message = null;
        try{
            job = entranceServer.getJob(realId);
        }catch(final Throwable t){
            message = Message.error("The job you just executed has ended. This interface no longer provides a query. It is recommended that you download the log file for viewing.(您刚刚执行的job已经结束，本接口不再提供查询，建议您下载日志文件进行查看)");
            message.setMethod("/api/entrance/" + id + "/log");
            return Message.messageToResponse(message);
        }
        if (job.isDefined()){
            logger.debug("开始获取 {} 的日志", job.get().getId());
            LogReader logReader = entranceServer.getEntranceContext().getOrCreateLogManager().getLogReader(realId);
            int fromLine = 0;
            int size = 100;
            boolean distinctLevel = true;
            if (req != null){
                String fromLineStr = req.getParameter("fromLine");
                String sizeStr = req.getParameter("size");
                if (StringUtils.isNotBlank(fromLineStr)){
                    fromLine = Integer.parseInt(fromLineStr) >= 0 ? Integer.parseInt(fromLineStr) : 0;
                }
                if (StringUtils.isNotBlank(sizeStr)){
                    size = Integer.parseInt(sizeStr) >= 0 ? Integer.parseInt(sizeStr) : 10000;
                }
                String distinctLevelStr = req.getParameter("distinctLevel");
                 if ("false".equals(distinctLevelStr)){
                     distinctLevel = false;
                 }
            }
            String[] logs = null;
            StringBuilder sb = null;
            Object retLog = null;
            int retFromLine = 0;
            try{
                if (distinctLevel){
                    logs = new String[4];
                    retFromLine =  logReader.readArray(logs, fromLine, size);
                    retLog = new ArrayList<String>(Arrays.asList(logs));
                }else{
                    sb = new StringBuilder();
                    retFromLine =  logReader.read(sb, fromLine, size);
                    retLog = sb.toString();
                }
            }catch(final IllegalArgumentException e){
                logger.error("为 {} 获取日志失败", job.get().getId());
                message = Message.ok();
                message.setMethod("/api/entrance/" + id + "/log");
                message.data("log","").data("execID", id).data("fromLine", retFromLine + fromLine);
                return Message.messageToResponse(message);
            }catch(final Exception e1){
                logger.error("为 {} 获取日志失败", job.get().getId(), e1);
                message = Message.error("Failed to get log information(获取日志信息失败)");
                message.setMethod("/api/entrance/" + id + "/log");
                message.data("log","").data("execID", id).data("fromLine", retFromLine + fromLine);
                return Message.messageToResponse(message);
            } finally {
                IOUtils.closeQuietly(logReader);
            }
            message = Message.ok();
            message.setMethod("/api/entrance/" + id + "/log");
            message.data("log",retLog).data("execID", id).data("fromLine", retFromLine + fromLine);
            logger.debug("获取 {} 日志成功", job.get().getId());
        }else{
            message = Message.error("Can't find execID(不能找到execID): " + id + "Corresponding job, can not get the corresponding log(对应的job，不能获得对应的日志)");
            message.setMethod("/api/entrance/" + id + "/log");
        }
        return Message.messageToResponse(message);
    }



//    @Override
//    @GET
//    @Path("/{taskID}/totalLog")
//    public Response totalLog(@PathParam("taskID")long taskID){
//        PersistenceManager persistenceManager = this.entranceServer.getEntranceContext().getOrCreatePersistenceManager();
//        Message message = null;
//        try {
//            Task task = persistenceManager.createPersistenceEngine().retrieve(taskID);
//            if (task instanceof RequestPersistTask){
//                RequestPersistTask requestPersistTask = (RequestPersistTask) task;
//                String logPath = requestPersistTask.getLogPath();
//                if (StringUtils.isNotEmpty(logPath)){
//                   try{
//                       Fs fs = FSFactory.getFs(new FsPath(logPath));
//                       fs.init(new HashMap<String, String>());
//                       InputStream inputStream = fs.read(new FsPath(logPath));
//                       byte[] bytes = new byte[1024];
//                       StringBuilder sb = new StringBuilder();
//                       while((inputStream.read(bytes)) != -1){
//                           sb.append(new String(bytes, "utf-8"));
//                       }
//                       logger.info("get taskID: {} log success", taskID  );
//                       message = Message.ok("获取全量日志成功");
//                       message.data("log",sb.toString());
//                   }catch (IOException e){
//                       logger.error("get taskID: {} log failed.", taskID, e);
//                       message = Message.error("请求获取日志信息失败，请稍后重试");
//                   }
//                }
//            }
//        } catch (ErrorException e) {
//            logger.error("retrieve {} task failed",taskID, e);
//            message = Message.error("请求获取日志信息失败，请稍后重试");
//        }
//        return Message.messageToResponse(message);
//    }

    @Override
    @GET
    @Path("/{id}/kill")
    public Response kill(@PathParam("id")String id, @QueryParam("taskID") long taskID) {
        String realId = ZuulEntranceUtils.parseExecID(id)[3];
        //通过jobid获取job,可能会由于job找不到而导致有looparray的报错,一旦报错的话，就可以将该任务直接置为Cancenlled
        Option<Job> job = Option.apply(null);
        try{
            job = entranceServer.getJob(realId);
        }catch(Exception e){
            logger.warn("can not find a job in entranceServer, will force to kill it", e);
            //如果在内存中找不到该任务，那么该任务可能已经完成了，或者就是重启导致的
            JobHistoryHelper.forceKill(taskID);
            Message message = Message.ok("强制杀死任务");
            message.setMethod("/api/entrance/" + id + "/kill");
            message.setStatus(0);
            return Message.messageToResponse(message);
        }
        Message message = null;
        if (job.isEmpty()){
            message = Message.error("Can't find execID(不能找到execID): " + id + "Corresponding job, can't kill(对应的job，不能进行kill)");
            message.setMethod("/api/entrance/" + id + "/kill");
            message.setStatus(1);
        }else{
            try{
                logger.info("begin to kill job {} ", job.get().getId());
                job.get().kill();
                message = Message.ok("Successfully killed the job(成功kill了job)");
                message.setMethod("/api/entrance/" + id + "/kill");
                message.setStatus(0);
                message.data("execID", id);
                //ensure the job's state is cancelled in database
                if (job.get() instanceof EntranceJob){
                    EntranceJob entranceJob = (EntranceJob)job.get();
                    Task task = entranceJob.getTask();
                    ((RequestPersistTask)task).setStatus("Cancelled");
                    this.entranceServer.getEntranceContext().getOrCreatePersistenceManager().createPersistenceEngine().updateIfNeeded(task);
                }
                logger.info("end to kill job {} ", job.get().getId());
            }catch(Throwable t){
                logger.error("kill job {} failed ", job.get().getId(), t);
                message = Message.error("An exception occurred while killing the job, kill failed(kill job的时候出现了异常，kill失败)");
                message.setMethod("/api/entrance/" + id + "/kill");
                message.setStatus(1);
            }
        }
        return Message.messageToResponse(message);
    }

    @Override
    @GET
    @Path("/{id}/pause")
    public Response pause(@PathParam("id")String id) {
        String realId = ZuulEntranceUtils.parseExecID(id)[3];
        Option<Job> job = entranceServer.getJob(realId);
        Message message = null;
        if (job.isEmpty()){
            message = Message.error("不能找到execID: " + id + "对应的job，不能进行pause");
            message.setMethod("/api/entrance/" + id + "/pause");
            message.setStatus(1);
        }else{
            try{
                //todo job pause 接口还未实现和给出
                //job.pause();
                logger.info("begin to pause job {} ", job.get().getId());
                message = Message.ok("成功pause了job");
                message.setStatus(0);
                message.data("execID", id);
                message.setMethod("/api/entrance/" + id + "/pause");
                logger.info("end to pause job {} ", job.get().getId());
            }catch(Throwable t){
                logger.info("pause job {} failed ", job.get().getId());
                message = Message.error("Abnormal when pausing job, pause failed(pause job的时候出现了异常，pause失败)");
                message.setMethod("/api/entrance/" + id + "/pause");
                message.setStatus(1);
            }
        }
        return Message.messageToResponse(message);
    }

    @Override
    @POST
    @Path("/backgroundservice")
    public Response backgroundservice(@Context HttpServletRequest req, Map<String, Object> json) {
        Message message = null;
        logger.info("Begin to get an execID");
        String backgroundType = (String) json.get("background");
        BackGroundService[] bgServices = entranceServer.getEntranceContext().getOrCreateBackGroundService();
        BackGroundService bgService =null;
        for (BackGroundService backGroundService : bgServices) {
            if (backgroundType.equals(backGroundService.serviceType())){
                bgService = backGroundService;
                break;
            }
        }
/*        Map<String,Object> executionCode = (Map<String, Object>) json.get("executionCode");
        String executionCodeJson = new Gson().toJson(executionCode);
        json.put("executionCode",executionCodeJson);*/
        json.put(TaskConstant.UMUSER, SecurityFilter.getLoginUsername(req));
        ServerEvent serverEvent = new ServerEvent();
        serverEvent.setData(json);
        ServerEvent operation = bgService.operation(serverEvent);
        String execID = entranceServer.execute(operation.getData());
        Task task = ((EntranceJob)entranceServer.getJob(execID).get()).getTask();
        Long taskID = ((RequestPersistTask)task).getTaskID();
        execID = ZuulEntranceUtils.generateExecID(execID, ((RequestPersistTask) task).getExecuteApplicationName(),
                Sender.getThisInstance(), ((RequestPersistTask) task).getRequestApplicationName());
        message = Message.ok();
        message.setMethod("/api/entrance/backgroundservice");
        message.data("execID",execID);
        message.data("taskID", taskID);
        logger.info("End to get an an execID: {}, taskID: {}", execID, taskID);
        return Message.messageToResponse(message);

    }
}
