package org.apache.linkis.metadatamanager.server;
import org.apache.linkis.DataWorkCloudApplication;
import org.apache.linkis.LinkisBaseServerApp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class LinkisMetadataManagerApplication {

    private static final Log logger = LogFactory.getLog(LinkisMetadataManagerApplication.class);

    public static void main(String[] args) throws ReflectiveOperationException {
        logger.info("Start to running LinkisMetadataManagerApplication");
        LinkisBaseServerApp.main(args);
    }
}
