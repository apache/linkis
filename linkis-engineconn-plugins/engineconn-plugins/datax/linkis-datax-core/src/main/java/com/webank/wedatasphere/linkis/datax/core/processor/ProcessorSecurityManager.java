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

package com.webank.wedatasphere.linkis.datax.core.processor;

import sun.security.util.SecurityConstants;

import java.awt.*;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FilePermission;
import java.net.SocketPermission;
import java.security.AccessControlException;
import java.security.Permission;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyPermission;

/**
 * @author davidhua
 * 2019/8/12
 */
@SuppressWarnings("restriction")
public class ProcessorSecurityManager extends SecurityManager {

    public static final String CAN_SET_SECURITY_MANAGER = "canSetSecurityManager";
    public static final String CAN_BUILD_SOCK = "canBuildSock";
    public static final String CAN_READ_PROPERTIES = "canWriteProps";
    public static final String CAN_RW_PROPERTIES = "canReadWriteProps";

    private static final String SET_SECURITY_MANAGER = "setSecurityManager";
    private static final String EXIT_JVM = "exitVM";
    private static final String QUEUE_PRINT_JOB = "queuePrintJob";
    private static final String MODIFY_THREAD_GROUP_PERMISSION = "modifyThreadGroup";
    private static final String MODIFY_THREAD_PERMISSION = "modifyThread";

    private Map<String, Boolean> switchMap = new HashMap<>();
    private String workDir;

    public ProcessorSecurityManager(String workDir){
        this.workDir = workDir;
    }

    public ProcessorSecurityManager(String workDir, Map<String, Boolean> switchMap){
        this.workDir = workDir;
        if(null != switchMap) {
            this.switchMap = switchMap;
        }
    }
    @Override
    public void checkRead(FileDescriptor fd) {
        checkIfStdDescriptor(fd);
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        checkIfStdDescriptor(fd);
    }


    @Override
    public void checkPermission(Permission perm) {
        if(perm instanceof AWTPermission){
            throw new AccessControlException("have no permission to load AWT");
        }else if(perm instanceof FilePermission){
            FilePermission permission = (FilePermission)perm;
            if(SecurityConstants.FILE_EXECUTE_ACTION.equals(permission.getActions())){
                throw new AccessControlException("have no permission to execute command");
            }
            checkFilePermission(permission.getName());
        }else if(perm instanceof SocketPermission){
            checkSocket((SocketPermission)perm);
        }else if(perm instanceof PropertyPermission){
            checkProps((PropertyPermission)perm);
        }else if(perm instanceof RuntimePermission){
            checkRuntime((RuntimePermission)perm);
        }
    }

    private void checkFilePermission(String file){
        if(!new File(file).getAbsolutePath().startsWith(workDir)){
            throw new AccessControlException("have no permission to : " + file);
        }
    }

    private void checkIfStdDescriptor(FileDescriptor fd){
        if(!fd.equals(FileDescriptor.out) && !fd.equals(FileDescriptor.in)
                && !fd.equals(FileDescriptor.err)) {
            throw new AccessControlException("have no permission to read fd : " + fd.toString());
        }
    }

    private void checkSocket(SocketPermission perm){
        if(!this.switchMap.getOrDefault(CAN_BUILD_SOCK, false)){
            throw new AccessControlException("have no permission to build a socket");
        }
    }


    private void checkProps(PropertyPermission perm){
        if(perm.getActions().equals(SecurityConstants.PROPERTY_READ_ACTION)
                && !this.switchMap.getOrDefault(CAN_READ_PROPERTIES, true)){
            throw new AccessControlException("have no permission to read property");
        }
        if(perm.getActions().equals(SecurityConstants.PROPERTY_RW_ACTION)
                && !this.switchMap.getOrDefault(CAN_RW_PROPERTIES, false)){
            throw new AccessControlException("have no permission to read/write property");
        }
    }

    private void checkRuntime(RuntimePermission perm){
        if(perm.getName().startsWith(EXIT_JVM)){
            throw new AccessControlException("have no permission to exit jvm");
        }
        if(perm.getName().equals(QUEUE_PRINT_JOB)){
            throw new AccessControlException("have no permission to queue print job");
        }
        if(perm.getName().equals(MODIFY_THREAD_GROUP_PERMISSION) || perm.getName().equals(MODIFY_THREAD_PERMISSION)){
            throw new AccessControlException("have no permission to modify thread/threadGroup");
        }
        if(perm.getName().equals(SET_SECURITY_MANAGER) &&
                !this.switchMap.getOrDefault(CAN_SET_SECURITY_MANAGER, false)){
            throw new AccessControlException("have no permission to set SecurityManager");
        }

    }

    public void setCanSetSecurityManager(boolean isCan){
        this.switchMap.put(CAN_SET_SECURITY_MANAGER, isCan);
    }

}
