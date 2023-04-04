package org.apache.linkis.storage;

import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.storage.resultset.ResultMetaData;

public class LineMetaData implements ResultMetaData {

    public String metaData = null;

    public LineMetaData(String metaData) {
        this.metaData = metaData;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public MetaData cloneMeta() {
        return new LineMetaData(metaData);
    }

}
