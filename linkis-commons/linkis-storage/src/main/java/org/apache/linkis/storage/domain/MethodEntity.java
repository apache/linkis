package org.apache.linkis.storage.domain;

import com.fasterxml.jackson.module.scala.ScalaObjectMapper$;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json4s.DefaultFormats;
import org.json4s.DefaultFormats$;
import org.json4s.jackson.JsonMethods$;
import org.json4s.jackson.Serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json4s.DefaultFormats;
import org.json4s.jackson.JsonMethods;

import java.lang.reflect.Type;
import java.lang.reflect.Type;

/**
 * Engine unique Id(engine唯一的Id)
 * <p>
 * Fs type(fs类型)
 * <p>
 * Create a user to start the corresponding jvm user(创建用户为对应启动的jvm用户)
 * <p>
 * Proxy user(代理用户)
 * <p>
 * client Ip for whitelist control(ip用于白名单控制)
 * <p>
 * Method name called(调用的方法名)
 * <p>
 * Method parameter(方法参数)
 */
public class MethodEntity {
    public long id;
    public String fsType;
    public String creatorUser;
    public String proxyUser;
    private String clientIp;
    public String methodName;
    public Object[] params;

    public MethodEntity(long id, String fsType, String creatorUser, String proxyUser, String clientIp, String methodName, Object[] params) {
        this.id = id;
        this.fsType = fsType;
        this.creatorUser = creatorUser;
        this.proxyUser = proxyUser;
        this.clientIp = clientIp;
        this.methodName = methodName;
        this.params = params;
    }

    public long getId() {
        return id;
    }

    public String getFsType() {
        return fsType;
    }

    public String getCreatorUser() {
        return creatorUser;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "id:" + id + ", methodName:" + methodName + ", fsType:" + fsType +
                ", creatorUser:" + creatorUser + ", proxyUser:" + proxyUser + ", clientIp:" + clientIp;
    }
}

