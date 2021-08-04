package com.webank.wedatasphere.linkis.storage.exception;

public enum StorageErrorCode {

    /**
     *
     */
    FS_NOT_INIT(53001,"please init first")
    ;


     StorageErrorCode(int errorCode, String message) {
         this.code = errorCode;
         this.message = message;
    }

    private int code;

    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
