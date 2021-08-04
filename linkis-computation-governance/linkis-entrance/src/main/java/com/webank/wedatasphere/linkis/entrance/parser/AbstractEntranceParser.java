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

import com.webank.wedatasphere.linkis.entrance.EntranceContext;
import com.webank.wedatasphere.linkis.entrance.EntranceParser;
import com.webank.wedatasphere.linkis.entrance.exception.EntranceErrorCode;
import com.webank.wedatasphere.linkis.entrance.exception.EntranceIllegalParamException;
import com.webank.wedatasphere.linkis.entrance.execute.EntranceJob;
import com.webank.wedatasphere.linkis.entrance.job.EntranceExecutionJob;
import com.webank.wedatasphere.linkis.entrance.persistence.PersistenceManager;
import com.webank.wedatasphere.linkis.governance.common.entity.job.JobRequest;
import com.webank.wedatasphere.linkis.governance.common.paser.CodeParser;
import com.webank.wedatasphere.linkis.governance.common.paser.CodeParserFactory;
import com.webank.wedatasphere.linkis.governance.common.paser.CodeType;
import com.webank.wedatasphere.linkis.governance.common.paser.EmptyCodeParser;
import com.webank.wedatasphere.linkis.governance.common.utils.GovernanceConstant;
import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant;
import com.webank.wedatasphere.linkis.manager.label.entity.Label;
import com.webank.wedatasphere.linkis.protocol.utils.TaskUtils;
import com.webank.wedatasphere.linkis.scheduler.queue.Job;
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public abstract class AbstractEntranceParser extends EntranceParser {

    private EntranceContext entranceContext;

    private static final Logger logger = LoggerFactory.getLogger(AbstractEntranceParser.class);

    @Override
    public EntranceContext getEntranceContext() {
        return entranceContext;
    }

    @Override
    public void setEntranceContext(EntranceContext entranceContext) {
        this.entranceContext = entranceContext;
    }


    protected EntranceJob createEntranceJob() {
        return new EntranceExecutionJob(getPersistenceManager());
    }

    protected PersistenceManager getPersistenceManager() {
        return null;
    }

    /**
     * Parse a jobReq into an executable job(将一个task解析成一个可执行的job)
     *  todo Parse to jobGroup
     * @param jobReq
     * @return
     */
    @Override
    public Job parseToJob(JobRequest jobReq) throws EntranceIllegalParamException {
        if (jobReq == null) {
            throw new EntranceIllegalParamException(20001, "JobReq can't be null");
        }
        EntranceJob job = createEntranceJob();
        job.setId(String.valueOf(jobReq.getId()));
        job.setJobRequest(jobReq);
        job.setUser(jobReq.getExecuteUser());
        Label<?> userCreateLabel = jobReq.getLabels().stream().filter(l -> l.getLabelKey().equalsIgnoreCase(LabelKeyConstant.USER_CREATOR_TYPE_KEY)).findFirst().orElseGet(null);
        if (null != userCreateLabel) {
            job.setCreator(userCreateLabel.getStringValue().split("\\-")[1]);
        } else {
            String msg = "JobRequest doesn't hava valid userCreator label. labels : " + BDPJettyServerHelper.gson().toJson(jobReq.getLabels());
            logger.error(msg);
            throw new EntranceIllegalParamException(EntranceErrorCode.LABEL_PARAMS_INVALID.getErrCode(), msg);
        }
        job.setParams(jobReq.getParams());
        //TODO 放置source到RequestTask的properties中，后续会进行优化
        Map<String, Object> properties = TaskUtils.getRuntimeMap(job.getParams());
        properties.put(GovernanceConstant.TASK_SOURCE_MAP_KEY(), jobReq.getSource());
        job.setEntranceListenerBus(entranceContext.getOrCreateEventListenerBus());
        job.setEntranceLogListenerBus(entranceContext.getOrCreateLogListenerBus());
        job.setEntranceContext(entranceContext);
        job.setListenerEventBus(null);
        job.setProgress(0f);
        job.setJobRequest(jobReq);
        Label<?> codeTypeLabel = jobReq.getLabels().stream().filter(l -> l.getLabelKey().equalsIgnoreCase(LabelKeyConstant.CODE_TYPE_KEY)).findFirst().orElseGet(null);
        if (null != codeTypeLabel) {
            CodeParser codeParser = CodeParserFactory.getCodeParser(CodeType.getType(codeTypeLabel.getStringValue()));
            if (null != codeParser) {
                job.setCodeParser(codeParser);
            } else {
                job.setCodeParser(new EmptyCodeParser());
            }
        }
        return job;
    }

}
