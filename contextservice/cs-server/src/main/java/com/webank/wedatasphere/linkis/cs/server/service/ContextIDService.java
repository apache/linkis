package com.webank.wedatasphere.linkis.cs.server.service;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;

/**
 * Created by patinousward on 2020/2/18.
 */
public abstract class ContextIDService extends AbstractService {

    public abstract String createContextID(ContextID contextID) throws CSErrorException;

    public abstract ContextID getContextID(String id) throws CSErrorException;

    public abstract void updateContextID(ContextID contextID) throws CSErrorException;

    public abstract void resetContextID(String id) throws CSErrorException;

    public abstract void removeContextID(String id) throws CSErrorException;
}
