package com.webank.wedatasphere.linkis.engine.flink.common;

import com.webank.wedatasphere.linkis.engine.flink.client.sql.operation.result.ResultSet;
import com.webank.wedatasphere.linkis.storage.resultset.table.TableMetaData;
import com.webank.wedatasphere.linkis.storage.resultset.table.TableRecord;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.types.Row;

import java.io.IOException;
import java.util.List;

public interface ResultListener {
    /**
     * 查询成功
     *
     * @param list
     */
    void resultNotify();



    /**
     * 查询失败
     *
     * @param status 任务状态
     */
    void error(int status);


    boolean isFinished();

    /**
     * 进度提示，每隔固定时间返回执行进度
     *
     * @param progress 进度信息，进度数值小于零表示队列已满，任务正在等待执行
     */
    void progress(float progress);

    /**
     * 提示信息
     *
     * @param message 提示信息
     */
    void message(List<String> message);


    void cancel();
}
