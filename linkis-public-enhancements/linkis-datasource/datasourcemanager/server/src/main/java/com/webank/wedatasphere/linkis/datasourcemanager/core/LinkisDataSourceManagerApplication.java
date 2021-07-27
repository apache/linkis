package com.webank.wedatasphere.linkis.datasourcemanager.core;
import com.webank.wedatasphere.linkis.DataWorkCloudApplication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class LinkisDataSourceManagerApplication {

    private static final Log logger = LogFactory.getLog(LinkisDataSourceManagerApplication.class);

    public static void main(String[] args) throws ReflectiveOperationException {
        logger.info("Start to running LinkisDataSourceManagerApplication");
        DataWorkCloudApplication.main(args);
    }
}
