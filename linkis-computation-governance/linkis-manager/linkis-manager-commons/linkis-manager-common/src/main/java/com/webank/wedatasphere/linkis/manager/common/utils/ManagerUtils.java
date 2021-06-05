package com.webank.wedatasphere.linkis.manager.common.utils;

import com.webank.wedatasphere.linkis.manager.common.conf.ManagerCommonConf;
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactory;
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import com.webank.wedatasphere.linkis.manager.label.entity.Label;
import org.apache.commons.lang.StringUtils;

import java.util.Map;


public class ManagerUtils {

    private final static LabelBuilderFactory labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory();


    public static <T> T getValueOrDefault(Map<String, Object> map, String key, T defaultValue) {
        Object value = defaultValue;
        if (null != map && null != map.get(key)) {
            value = map.get(key);
        }
        return (T) value;
    }

    public static String getAdminUser() {

        if (StringUtils.isNotBlank(ManagerCommonConf.DEFAULT_ADMIN().getValue())) {
            return ManagerCommonConf.DEFAULT_ADMIN().getValue();
        }
        return System.getProperty("user.name");
    }

    public static Label<?> persistenceLabelToRealLabel(Label<?> label) {
        return labelFactory.createLabel(label.getLabelKey(),label.getValue());
    }


}
