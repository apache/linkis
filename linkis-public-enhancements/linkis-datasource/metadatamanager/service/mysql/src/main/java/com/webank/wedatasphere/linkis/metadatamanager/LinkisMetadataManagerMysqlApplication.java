package com.webank.wedatasphere.linkis.metadatamanager;

import com.webank.wedatasphere.linkis.DataWorkCloudApplication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LinkisMetadataManagerMysqlApplication {
    private static final Log logger = LogFactory.getLog(LinkisMetadataManagerMysqlApplication.class);

    public static void main(String[] args) throws ReflectiveOperationException {
        logger.info("Start to running LinkisMetadataManagerMysqlApplication");
        DataWorkCloudApplication.main(args);
    }
}
