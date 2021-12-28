/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.bml.restful;

import org.apache.linkis.bml.common.BmlAuthorityException;
import org.apache.linkis.bml.common.Constant;
import org.apache.linkis.bml.service.ResourceService;
import org.apache.linkis.server.security.SecurityFilter;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RestfulUtils {


    private static final Long SECOND = 1000L;
    private static final Long MINUTE = 60 * SECOND;
    private static final Long HOUR = 60 * MINUTE;
    public  static final Long DAY = 24 * HOUR;
    private static final Long MONTH = 30 * DAY;
    private static final Long YEAR = 365 * DAY;


    public static String getUserName(HttpServletRequest request)throws BmlAuthorityException{
        String user;
        try{
            user = SecurityFilter.getLoginUsername(request);
        }catch(final Exception e){
            throw new BmlAuthorityException();
        }
        return user;
    }

    /**
     * 用于检查用户user是否有权限对resourceId对应的资源有修改权限
     * @param user 传入的用户名
     * @param resourceId resourceId
     * @param resourceService service服务层
     * @return
     */
    public static boolean canAccessResource(String user, String resourceId,
                                            ResourceService resourceService){
        //String realUser = resourceService.getOwner(resourceId);
        //return user.equals(realUser);
        //todo
        return true;
    }

    public static String getExpireTime(Date createTime, String expireType, String expireTime){

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constant.TIME_FORMAT);
        String retTime = null;

        if ("date".equals(expireType)){
            return expireTime;
        }else{
            int num = Integer.parseInt(expireTime.substring(0, expireTime.length() - 1));
            switch (expireTime.charAt(expireTime.length() - 1)){
                case 'y': retTime = simpleDateFormat.format(new Date(createTime.getTime() + num * YEAR));
                    break;
                case 'M': retTime = simpleDateFormat.format(new Date(createTime.getTime() + num * MONTH));
                    break;
                case 'd': retTime = simpleDateFormat.format(new Date(createTime.getTime() + num * DAY));
                    break;
                case 'H': retTime = simpleDateFormat.format(new Date(createTime.getTime() + num * HOUR));
                    break;
                case 'm': retTime = simpleDateFormat.format(new Date(createTime.getTime() + num * MINUTE));
                    break;
                default: retTime = simpleDateFormat.format(new Date(createTime.getTime() + 10 * DAY));
            }
        }
        return retTime;
    }



}
