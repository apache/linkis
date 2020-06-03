package com.webank.wedatasphere.linkis.cs.highavailable.ha;

import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;

public interface BackupInstanceGenerator {

    String getBackupInstance(String haID) throws CSErrorException;

    String chooseBackupInstance(String mainInstance) throws CSErrorException;

}
