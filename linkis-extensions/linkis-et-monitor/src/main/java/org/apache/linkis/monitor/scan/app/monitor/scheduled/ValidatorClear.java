/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.monitor.scan.app.monitor.scheduled;

import org.apache.linkis.monitor.scan.app.monitor.config.MonitorConfig;
import org.apache.linkis.monitor.scan.app.monitor.until.ThreadUtils;
import org.apache.linkis.monitor.scan.utils.log.LogUtils;
import org.slf4j.Logger;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
/***
 * Task: clean up linkis_et_validator_checkinfo data
 */

@Component
@PropertySource(value = "classpath:linkis-et-monitor.properties", encoding = "UTF-8")
public class ValidatorClear {

    private static final Logger logger = LogUtils.stdOutLogger();

    @Scheduled(cron = "${linkis.monitor.clear.validator.cron}")
    public void ValidatorClear() {
        logger.info("Start to clear_validator_record shell");
        List<String> cmdlist = new ArrayList<>();
        cmdlist.add("sh");
        cmdlist.add(MonitorConfig.shellPath + "clear_validator_record.sh");
        logger.info("clear_validator_record  shell command {}", cmdlist);
        String exec = ThreadUtils.run(cmdlist, "clear_validator_record.sh");
        logger.info("shell log  {}", exec);
        logger.info("End to clear_validator_record shell ");
    }
}
