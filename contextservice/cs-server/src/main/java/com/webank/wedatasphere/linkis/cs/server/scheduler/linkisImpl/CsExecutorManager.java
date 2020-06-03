package com.webank.wedatasphere.linkis.cs.server.scheduler.linkisImpl;

import com.webank.wedatasphere.linkis.scheduler.executer.Executor;
import com.webank.wedatasphere.linkis.scheduler.executer.ExecutorManager;
import com.webank.wedatasphere.linkis.scheduler.listener.ExecutorListener;
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEvent;
import scala.Option;
import scala.Some;
import scala.concurrent.duration.Duration;

/**
 * Created by patinousward on 2020/2/18.
 */
public class CsExecutorManager extends ExecutorManager {

    @Override
    public void setExecutorListener(ExecutorListener executorListener) {

    }

    @Override
    public Executor createExecutor(SchedulerEvent event) {
        return new CsExecutor();
    }

    @Override
    public Option<Executor> askExecutor(SchedulerEvent event) {
        return new Some<>(createExecutor(event));
    }

    @Override
    public Option<Executor> askExecutor(SchedulerEvent event, Duration wait) {
        return askExecutor(event);
    }

    @Override
    public Option<Executor> getById(long id) {
        return new Some<>(null);
    }

    @Override
    public Executor[] getByGroup(String groupName) {
        return new Executor[0];
    }

    @Override
    public void delete(Executor executor) {

    }

    @Override
    public void shutdown() {

    }
}
