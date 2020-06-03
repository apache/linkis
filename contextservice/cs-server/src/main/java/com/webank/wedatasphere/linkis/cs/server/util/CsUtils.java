package com.webank.wedatasphere.linkis.cs.server.util;

import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.serialize.helper.ContextSerializationHelper;
import com.webank.wedatasphere.linkis.cs.common.serialize.helper.SerializationHelper;

/**
 * Created by patinousward on 2020/2/22.
 */
public class CsUtils {

    public static final SerializationHelper SERIALIZE = ContextSerializationHelper.getInstance();

    public static String serialize(Object o) throws CSErrorException {
        if (o instanceof String){
            return (String)o;
        }
        return SERIALIZE.serialize(o);
    }

}
