package org.apache.linkis.metadatamanager;

import org.apache.linkis.DataWorkCloudApplication;
import org.apache.linkis.LinkisBaseServerApp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LinkisMetadataManagerHiveApplication {
    private static final Log logger = LogFactory.getLog(LinkisMetadataManagerHiveApplication.class);

    public static void main(String[] args) throws ReflectiveOperationException {
        logger.info("Start to running LinkisMetadataManagerHiveApplication");
        LinkisBaseServerApp.main(args);
    }
}
