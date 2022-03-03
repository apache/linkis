package org.apache.linkis.server.utils;

import org.apache.linkis.proxy.ProxyUserEntity;
import org.apache.linkis.server.security.ProxyUserSSOUtils;
import org.apache.linkis.server.security.SecurityFilter;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Option;

public class ModuleUserUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleUserUtils.class);

    /**
     * get ProxyUserEntity 1.if not support proxy return loginUser 2.if proxy user exist return
     * proxyUser and loginUser
     *
     * @param httpServletRequest
     * @return
     */
    public static ProxyUserEntity getProxyUserEntity(HttpServletRequest httpServletRequest) {
        String loginUser = SecurityFilter.getLoginUsername(httpServletRequest);
        Option<String> proxyUserUsername =
                ProxyUserSSOUtils.getProxyUserUsername(httpServletRequest);
        ProxyUserEntity proxyUserEntity = new ProxyUserEntity();
        proxyUserEntity.setUsername(loginUser);
        if (proxyUserUsername.isDefined()) {
            proxyUserEntity.setProxyUser(proxyUserUsername.get());
        }
        return proxyUserEntity;
    }

    /**
     * get ProxyUserEntity and print operation log
     *
     * @param httpServletRequest
     * @param msg
     * @return
     */
    public static ProxyUserEntity getProxyUserEntity(
            HttpServletRequest httpServletRequest, String msg) {
        ProxyUserEntity proxyUserEntity = getProxyUserEntity(httpServletRequest);
        LOGGER.info(
                "user {} proxy to {} operation {}",
                proxyUserEntity.getUsername(),
                proxyUserEntity.getProxyUser(),
                msg);
        return proxyUserEntity;
    }

    public static String getOperationUser(HttpServletRequest httpServletRequest) {
        ProxyUserEntity proxyUserEntity = getProxyUserEntity(httpServletRequest);
        if (proxyUserEntity.isProxyMode()) {
            return proxyUserEntity.getProxyUser();
        } else {
            return proxyUserEntity.getUsername();
        }
    }

    /**
     * get operation user and print log
     *
     * @param httpServletRequest
     * @param msg
     * @return
     */
    public static String getOperationUser(HttpServletRequest httpServletRequest, String msg) {
        ProxyUserEntity proxyUserEntity = getProxyUserEntity(httpServletRequest, msg);
        if (proxyUserEntity.isProxyMode()) {
            return proxyUserEntity.getProxyUser();
        } else {
            return proxyUserEntity.getUsername();
        }
    }
}
