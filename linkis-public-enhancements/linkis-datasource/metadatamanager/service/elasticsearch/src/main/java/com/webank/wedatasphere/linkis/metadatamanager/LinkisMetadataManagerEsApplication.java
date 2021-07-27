package com.webank.wedatasphere.linkis.metadatamanager;

import com.webank.wedatasphere.linkis.DataWorkCloudApplication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LinkisMetadataManagerEsApplication {
    private static final Log logger = LogFactory.getLog(LinkisMetadataManagerEsApplication.class);

    public static void main(String[] args) throws ReflectiveOperationException {
        logger.info("Start to running LinkisMetadataManagerEsApplication");
        DataWorkCloudApplication.main(args);
    }
}
