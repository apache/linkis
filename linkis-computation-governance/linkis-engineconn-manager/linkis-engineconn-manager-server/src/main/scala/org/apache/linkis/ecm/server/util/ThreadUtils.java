package org.apache.linkis.ecm.server.util;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.utils.Utils;

import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

import java.util.*;
import java.util.concurrent.*;

import scala.concurrent.ExecutionContextExecutorService;

import org.slf4j.Logger;

public class ThreadUtils extends ApplicationContextEvent {

    private static final Logger logger = LoggerFactory.getLogger(ThreadUtils.class);
    public static final String shellPath = Configuration.getLinkisHome() + "/admin/";

    public static ExecutionContextExecutorService executors =
            Utils.newCachedExecutionContext(5, "alert-pool-thread-", false);

    public ThreadUtils(ApplicationContext source) {
        super(source);
    }

    public static String run(List<String> cmdList, String shellName) {
        FutureTask future = new FutureTask(() -> Utils.exec(cmdList.toArray(new String[2]), -1));
        executors.submit(future);
        String msg = "";
        try {
            msg = future.get(30, TimeUnit.MINUTES).toString();
        } catch (TimeoutException e) {
            logger.info("shell执行超时 {}", shellName);
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Thread error msg {}", e.getMessage());
        }
        return msg;
    }
}
