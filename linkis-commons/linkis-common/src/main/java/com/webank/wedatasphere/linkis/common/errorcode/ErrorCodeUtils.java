package com.webank.wedatasphere.linkis.common.errorcode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.webank.wedatasphere.linkis.common.errorcode.LinkisFrameErrorCodeSummary.VALIDATE_ERROR_CODE_FAILED;


public class ErrorCodeUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorCodeUtils.class);

    public static void validateErrorCode(int errCode, int startCode, int endCode) {
        if (errCode < startCode || errCode > endCode) {
            LOGGER.error("You error code validate failed, please fix it and reboot");
            System.exit(VALIDATE_ERROR_CODE_FAILED.getErrorCode());
        }
    }
}
