package com.webank.wedatasphere.linkis.bml;

import com.webank.wedatasphere.linkis.DataWorkCloudApplication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LinkisBMLApplication {

    private static final Log logger = LogFactory.getLog(LinkisBMLApplication.class);

    public static void main(String[] args) throws ReflectiveOperationException {
        logger.info("Start to running LinkisBMLApplication");
        DataWorkCloudApplication.main(args);
    }
}
