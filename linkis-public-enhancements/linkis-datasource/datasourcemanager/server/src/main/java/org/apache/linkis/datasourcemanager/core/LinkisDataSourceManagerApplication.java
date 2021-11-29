package org.apache.linkis.datasourcemanager.core;
import org.apache.linkis.DataWorkCloudApplication;
import org.apache.linkis.LinkisBaseServerApp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class LinkisDataSourceManagerApplication {

    private static final Log logger = LogFactory.getLog(LinkisDataSourceManagerApplication.class);

    public static void main(String[] args) throws ReflectiveOperationException {
        logger.info("Start to running LinkisDataSourceManagerApplication");
        LinkisBaseServerApp.main(args);
    }
}
