package com.webank.wedatasphere.linkis.metadatamanager;

import com.webank.wedatasphere.linkis.DataWorkCloudApplication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LinkisMetadataManagerKafkaApplication {
    private static final Log logger = LogFactory.getLog(LinkisMetadataManagerKafkaApplication.class);

    public static void main(String[] args) throws ReflectiveOperationException {
        logger.info("Start to running LinkisMetadataManagerKafkaApplication");
        DataWorkCloudApplication.main(args);
    }
}
