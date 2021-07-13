package com.webank.wedatasphere.linkis.engineplugin.hive.serde;


import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.ByteStream;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.SerDeUtils;
import org.apache.hadoop.hive.serde2.lazy.LazySerDeParameters;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.io.Writable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CustomerDelimitedJSONSerDe extends LazySimpleSerDe2 {
    public static final Logger LOG = LoggerFactory.getLogger(CustomerDelimitedJSONSerDe.class.getName());

    public CustomerDelimitedJSONSerDe() throws SerDeException {
    }

    /**
     * Not implemented.
     */
    @Override
    public Object doDeserialize(Writable field) throws SerDeException {
        LOG.error("DelimitedJSONSerDe cannot deserialize.");
        throw new SerDeException("DelimitedJSONSerDe cannot deserialize.");
    }

    @Override
    protected void serializeField(ByteStream.Output out, Object obj, ObjectInspector objInspector,
                                  LazySerDeParameters serdeParams) throws SerDeException {
        if (!objInspector.getCategory().equals(ObjectInspector.Category.PRIMITIVE) || (objInspector.getTypeName().equalsIgnoreCase(serdeConstants.BINARY_TYPE_NAME))) {
            //do this for all complex types and binary
            try {
                serialize(out, SerDeUtils.getJSONString(obj, objInspector, serdeParams.getNullSequence().toString()),
                        PrimitiveObjectInspectorFactory.javaStringObjectInspector, serdeParams.getSeparators(),
                        1, serdeParams.getNullSequence(), serdeParams.isEscaped(), serdeParams.getEscapeChar(),
                        serdeParams.getNeedsEscape());

            } catch (IOException e) {
                throw new SerDeException(e);
            }

        } else {
            //primitives except binary
            super.serializeField(out, obj, objInspector, serdeParams);
        }
    }
}

