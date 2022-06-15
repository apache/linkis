package org.apache.linkis;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class MessageJava {


    @ApiModelProperty(value = "方法名")
    private String method;
    @ApiModelProperty(value = "状态")
    private String status;         //-1 no login, 0 success, 1 error, 2 validate failed, 3 auth failed, 4 warning
    @ApiModelProperty(value = "描述")
    private String message;
    @ApiModelProperty(value = "返回结果集")
    private Object data;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
