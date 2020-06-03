package com.webank.wedatasphere.linkis.cs.highavailable;

import com.webank.wedatasphere.linkis.cs.common.entity.source.HAContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.highavailable.ha.BackupInstanceGenerator;
import com.webank.wedatasphere.linkis.cs.highavailable.ha.ContextHAChecker;
import com.webank.wedatasphere.linkis.cs.highavailable.ha.ContextHAIDGenerator;

/**
 *
 * @Author alexyang
 * @Date 2020/2/16
 */
public abstract class AbstractContextHAManager implements ContextHAManager {

    public abstract ContextHAIDGenerator getContextHAIDGenerator();

    public abstract ContextHAChecker getContextHAChecker();

    public abstract BackupInstanceGenerator getBackupInstanceGenerator();

    public abstract HAContextID convertProxyHAID(HAContextID contextID) throws CSErrorException;
}
