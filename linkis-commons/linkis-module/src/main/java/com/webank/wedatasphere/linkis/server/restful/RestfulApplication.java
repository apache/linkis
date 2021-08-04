/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.server.restful;

import com.webank.wedatasphere.linkis.server.conf.ServerConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;


public class RestfulApplication extends ResourceConfig {

    private static final Log logger = LogFactory.getLog(RestfulApplication.class);

    public RestfulApplication() throws ClassNotFoundException {
        register(JacksonFeature.class);
        register(JacksonJsonProvider.class);
        register(MultiPartFeature.class);
        String registerClasses = ServerConfiguration.BDP_SERVER_RESTFUL_REGISTER_CLASSES().acquireNew();
        if(StringUtils.isNotBlank(registerClasses)) {
            for(String clazz : registerClasses.split(",")) {
                logger.info("register " + clazz);
                register(Class.forName(clazz, true, Thread.currentThread().getContextClassLoader()));
            }
        }
        String packages = ServerConfiguration.BDP_SERVER_RESTFUL_SCAN_PACKAGES().acquireNew();
        if(StringUtils.isNotBlank(packages)) {
            logger.info("packages " + packages);
            packages(packages.split(","));
        }
    }
}
