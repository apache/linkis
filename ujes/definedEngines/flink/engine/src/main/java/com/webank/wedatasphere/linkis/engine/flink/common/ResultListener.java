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

    void setJobid(String jobid);


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
