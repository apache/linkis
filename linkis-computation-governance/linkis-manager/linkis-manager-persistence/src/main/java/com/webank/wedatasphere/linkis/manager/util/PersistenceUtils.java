package com.webank.wedatasphere.linkis.manager.util;

import com.webank.wedatasphere.linkis.manager.common.entity.persistence.PersistenceLabel;
import com.webank.wedatasphere.linkis.manager.entity.Tunple;
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactory;
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import com.webank.wedatasphere.linkis.manager.label.entity.Label;
import com.webank.wedatasphere.linkis.manager.label.utils.LabelUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class PersistenceUtils {

    private final static LabelBuilderFactory labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory();

    public static PersistenceLabel setValue(PersistenceLabel persistenceLabel) {
        Label<?> label = labelFactory.createLabel(persistenceLabel.getLabelKey(), persistenceLabel.getStringValue());
        if (label.getValue() instanceof Map) {
            persistenceLabel.setValue(LabelUtils.Jackson.fromJson(persistenceLabel.getStringValue(), Map.class));
        }
        return persistenceLabel;
    }

    public static Tunple<String, Map<String, String>> entryToTunple(PersistenceLabel label) {
        return new Tunple<>(label.getLabelKey(), label.getValue());
    }

    public static boolean valueListIsEmpty(List<Map<String, String>> valueList) {
        return CollectionUtils.isEmpty(valueList)
                || CollectionUtils.isEmpty(valueList.stream().filter(MapUtils::isNotEmpty).collect(Collectors.toList()));
    }

    public static List<Map<String, String>> filterEmptyValueList(List<Map<String, String>> valueList) {
        return valueList.stream().filter(MapUtils::isNotEmpty).collect(Collectors.toList());
    }

    public static boolean KeyValueMapIsEmpty(Map<String, Map<String, String>> keyValueMap) {
        return MapUtils.isEmpty(keyValueMap)
                || MapUtils.isEmpty(keyValueMap.entrySet().stream().filter(e -> MapUtils.isNotEmpty(e.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public static Map<String, Map<String, String>> filterEmptyKeyValueMap(Map<String, Map<String, String>> keyValueMap) {
        return keyValueMap.entrySet().stream().filter(e -> MapUtils.isNotEmpty(e.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static boolean persistenceLabelListIsEmpty(List<PersistenceLabel> persistenceLabelList) {
        return CollectionUtils.isEmpty(persistenceLabelList) || CollectionUtils.isEmpty(persistenceLabelList.stream().filter(l -> MapUtils.isNotEmpty(l.getValue())).collect(Collectors.toList()));
    }

    public static List<PersistenceLabel> filterEmptyPersistenceLabelList(List<PersistenceLabel> persistenceLabelList) {
        return persistenceLabelList.stream().filter(e -> MapUtils.isNotEmpty(e.getValue())).collect(Collectors.toList());
    }

}
