package com.webank.wedatasphere.linkis.cs.server.scheduler.linkisImpl;

import com.webank.wedatasphere.linkis.protocol.engine.EngineState$;
import com.webank.wedatasphere.linkis.scheduler.executer.*;
import scala.Enumeration;

import java.io.IOException;

/**
 * Created by patinousward on 2020/2/22.
 */
public class CsExecutor implements Executor {

    private long id;
    private Enumeration.Value state;

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public ExecuteResponse execute(ExecuteRequest executeRequest) {
        //httpjob执行的地方
        try {
            if (executeRequest instanceof CsExecuteRequest) {
                CsExecuteRequest request = (CsExecuteRequest) executeRequest;
                request.getConsumer().accept(request.get());
            }
            return new SuccessExecuteResponse();
        } catch (Exception e) {
            return new ErrorExecuteResponse(e.getMessage(), e);
        }
    }

    @Override
    public EngineState$.Value state() {
        return this.state;
    }

    @Override
    public ExecutorInfo getExecutorInfo() {
        return new ExecutorInfo(id, state);
    }

    @Override
    public void close() throws IOException {
    }
}
