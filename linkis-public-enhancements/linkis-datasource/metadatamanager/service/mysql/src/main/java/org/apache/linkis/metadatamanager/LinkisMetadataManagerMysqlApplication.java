package org.apache.linkis.metadatamanager;

import org.apache.linkis.DataWorkCloudApplication;
import org.apache.linkis.LinkisBaseServerApp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LinkisMetadataManagerMysqlApplication {
    private static final Log logger = LogFactory.getLog(LinkisMetadataManagerMysqlApplication.class);

    public static void main(String[] args) throws ReflectiveOperationException {
        logger.info("Start to running LinkisMetadataManagerMysqlApplication");
        LinkisBaseServerApp.main(args);
    }
}
