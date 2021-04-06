package com.webank.wedatasphere.linkis.engine.impala.client.util;


import com.webank.wedatasphere.linkis.engine.impala.client.ResultListener;
import com.webank.wedatasphere.linkis.engine.impala.client.exception.ExceptionCode;
import com.webank.wedatasphere.linkis.engine.impala.client.exception.SubmitException;
import org.apache.hive.service.cli.thrift.TStatus;

public class ImpalaThriftUtil {
    private static final char[] hexCode = "0123456789abcdef".toCharArray();

    /**
     * 检查返回状态
     *
     * @param status
     * @param resultListener 用于接收信息
     * @throws SubmitException
     */
    public static void checkStatus(TStatus status, ResultListener resultListener) throws SubmitException {
        switch (status.getStatusCode()) {

            case STILL_EXECUTING_STATUS:
                throw SubmitException.of(ExceptionCode.StillRunningError);
            case ERROR_STATUS:
                throw SubmitException.of(ExceptionCode.ExecutionError, status.getErrorMessage());
            case INVALID_HANDLE_STATUS:
                throw SubmitException.of(ExceptionCode.InvalidHandleError);
            case SUCCESS_WITH_INFO_STATUS:
                if (resultListener != null) {
                    resultListener.message(status.getInfoMessages());
                }
                break;
            case SUCCESS_STATUS:
        }
    }

    /**
     * 检查返回状态
     *
     * @param status
     * @throws SubmitException
     */
    public static void checkStatus(TStatus status) throws SubmitException {
        checkStatus(status,null);
    }

    /*
     * impala 输出id的格式
     */
    public static String printUniqueId(byte[] b) {
        StringBuilder sb = new StringBuilder(":");
        for (int i = 0; i < 8; ++i) {
            sb.append(hexCode[(b[15 - i] >> 4) & 0xF]);
            sb.append(hexCode[(b[15 - i] & 0xF)]);
            sb.insert(0, hexCode[(b[i] & 0xF)]);
            sb.insert(0, hexCode[(b[i] >> 4) & 0xF]);
        }
        return sb.toString();
    }
}
