package org.apache.linkis.manager.label.entity.cache;

import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.entity.Feature;
import org.apache.linkis.manager.label.entity.GenericLabel;
import org.apache.linkis.manager.label.entity.annon.ValueSerialNum;

import java.util.HashMap;

public class CacheLabel extends GenericLabel {
    public CacheLabel() {
        setLabelKey(LabelKeyConstant.CACHE_KEY);
    }

    @Override
    public Feature getFeature() {
        return Feature.OPTIONAL;
    }

    @ValueSerialNum(1)
    public void setReadCacheBefore(String readCacheBefore) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put("readCacheBefore", readCacheBefore);
    }

    public String getReadCacheBefore() {
        if (null != getValue().get("readCacheBefore")) {
            return getValue().get("readCacheBefore");
        }
        return null;
    }

    @ValueSerialNum(0)
    public void setCacheExpireAfter(String cacheExpireAfter) {
        if (null == getValue()) {
            setValue(new HashMap<>());
        }
        getValue().put("cacheExpireAfter", cacheExpireAfter);
    }

    public String getCacheExpireAfter(){
        if (null != getValue().get("cacheExpireAfter")) {
            return getValue().get("cacheExpireAfter");
        }
        return null;
    }
}
