package com.webank.wedatasphere.linkis.metadata;

import com.webank.wedatasphere.linkis.DataWorkCloudApplication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LinkisDataSourceApplication {

    private static final Log logger = LogFactory.getLog(LinkisDataSourceApplication.class);

    public static void main(String[] args) throws ReflectiveOperationException {
        logger.info("Start to running LinkisDataSourceApplication");
        DataWorkCloudApplication.main(args);
    }
}
