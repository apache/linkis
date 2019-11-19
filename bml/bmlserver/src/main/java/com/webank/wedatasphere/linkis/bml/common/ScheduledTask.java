package com.webank.wedatasphere.linkis.bml.common;

import com.webank.wedatasphere.linkis.bml.conf.BmlServerConfiguration;
import com.webank.wedatasphere.linkis.bml.service.ResourceService;
import com.webank.wedatasphere.linkis.bml.service.VersionService;
import com.webank.wedatasphere.linkis.common.utils.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

/**
 * created by cooperyang on 2019/6/4
 * Description:
 */
@Component
public class ScheduledTask {


    @Autowired
    private ResourceService resourceService;

    @Autowired
    private VersionService versionService;

    private Logger logger = LoggerFactory.getLogger(ScheduledTask.class);

    private class CleanExpiredThread implements Runnable{
        @Override
        public void run() {
//            resourceService.cleanExpiredResources();
//            versionService.cleanExpiredVersions();
        }
    }

    @PostConstruct
    public void init(){
        logger.info("Schedule Task is init");
        CleanExpiredThread cleanExpiredThread = new CleanExpiredThread();
        Utils.defaultScheduler().scheduleAtFixedRate(cleanExpiredThread, 10,
                                                     ((Number)BmlServerConfiguration.BML_CLEAN_EXPIRED_TIME().getValue()).intValue(), TimeUnit.SECONDS);
    }

}
