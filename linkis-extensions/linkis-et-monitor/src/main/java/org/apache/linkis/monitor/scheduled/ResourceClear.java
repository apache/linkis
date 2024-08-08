package org.apache.linkis.monitor.scheduled;

import org.apache.linkis.monitor.config.MonitorConfig;
import org.apache.linkis.monitor.until.ThreadUtils;
import org.apache.linkis.monitor.utils.log.LogUtils;
import org.slf4j.Logger;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@PropertySource(value = "classpath:linkis-et-monitor.properties", encoding = "UTF-8")
public class ResourceClear {
    private static final Logger logger = LogUtils.stdOutLogger();
    @Scheduled(cron = "${linkis.monitor.clear.resource.reset.cron:0 30 18 * * ?}")
    public void ResourceReset() {
        logger.info("Start to clear_resource_set shell");
        List<String> cmdlist = new ArrayList<>();
        cmdlist.add("sh");
        cmdlist.add(MonitorConfig.shellPath + "clear_resource_set.sh");
        logger.info("clear_resource_set  shell command {}", cmdlist);
        String exec = ThreadUtils.run(cmdlist, "clear_resource_set.sh");
        logger.info("shell log  {}", exec);
        logger.info("End to clear_resource_set shell ");
    }
}
