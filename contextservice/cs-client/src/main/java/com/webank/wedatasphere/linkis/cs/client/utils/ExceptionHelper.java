package com.webank.wedatasphere.linkis.cs.client.utils;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;

/**
 * created by cooperyang on 2020/2/19
 * Description:
 */
public class ExceptionHelper {
    public static void throwErrorException(int errCode, String errMsg, Throwable t)throws ErrorException {
        ErrorException errorException = new ErrorException(errCode, errMsg);
        errorException.initCause(t);
        throw errorException;
    }
}
