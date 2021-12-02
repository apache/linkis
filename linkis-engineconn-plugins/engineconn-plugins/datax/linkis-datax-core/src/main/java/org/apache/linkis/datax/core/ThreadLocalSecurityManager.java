/*
 *
 *  Copyright 2020 WeBank
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.datax.core;


import java.io.FileDescriptor;
import java.security.AccessControlException;
import java.security.Permission;

/**
 * @author davidhua
 * 2019/8/13
 */
public class ThreadLocalSecurityManager extends SecurityManager{
    private static final String SET_SECURITY_MANAGER = "setSecurityManager";

    private ThreadLocal<SecurityManager> threadSecurityManager = new ThreadLocal<>();
    private ThreadLocal<String> keyCode = new ThreadLocal<>();


    @Override
    public void checkRead(FileDescriptor fd) {
        if(null != threadSecurityManager.get()) {
            threadSecurityManager.get().checkRead(fd);
        }
    }

    @Override
    public void checkWrite(FileDescriptor fd){
        if(null != threadSecurityManager.get()) {
            threadSecurityManager.get().checkWrite(fd);
        }
    }

    @Override
    public void checkPermission(Permission perm) {
        if(perm instanceof RuntimePermission && perm.getName().equals(SET_SECURITY_MANAGER)){
            throw new AccessControlException("have no permission to set SecurityManager");
        }
        if(null != threadSecurityManager.get()) {
            threadSecurityManager.get().checkPermission(perm);
        }
    }

    public void setThreadSecurityManager(Object invoker, SecurityManager manager){
        if(null == threadSecurityManager.get()) {
            String hashCode = String.valueOf(invoker.hashCode());
            keyCode.set(hashCode);
            threadSecurityManager.set(manager);
        }
    }

    public void removeThreadSecurityManager(Object invoker){
         String hashCode = String.valueOf(invoker.hashCode());
         String code = keyCode.get();
         if(code.equals(hashCode)){
             keyCode.remove();
             threadSecurityManager.remove();
         }
    }
}
