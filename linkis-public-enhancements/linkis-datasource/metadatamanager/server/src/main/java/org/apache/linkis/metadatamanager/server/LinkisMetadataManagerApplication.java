package com.webank.wedatasphere.linkis.metadatamanager.server;
import com.webank.wedatasphere.linkis.DataWorkCloudApplication;
import com.webank.wedatasphere.linkis.LinkisBaseServerApp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class LinkisMetadataManagerApplication {

    private static final Log logger = LogFactory.getLog(LinkisMetadataManagerApplication.class);

    public static void main(String[] args) throws ReflectiveOperationException {
        logger.info("Start to running LinkisMetadataManagerApplication");
        LinkisBaseServerApp.main(args);
    }
}
