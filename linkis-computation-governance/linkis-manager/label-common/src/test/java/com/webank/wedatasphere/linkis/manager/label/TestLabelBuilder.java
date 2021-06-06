package com.webank.wedatasphere.linkis.manager.label;

import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactory;
import com.webank.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import com.webank.wedatasphere.linkis.manager.label.builder.factory.StdLabelBuilderFactory;
import com.webank.wedatasphere.linkis.manager.label.entity.Label;
import com.webank.wedatasphere.linkis.manager.label.entity.node.AliasServiceInstanceLabel;
import com.webank.wedatasphere.linkis.manager.label.exception.LabelErrorException;


public class TestLabelBuilder {

    public static void main(String[] args) throws LabelErrorException {
        LabelBuilderFactory labelBuilderFactory = LabelBuilderFactoryContext.getLabelBuilderFactory();
        Label<?> engineType = labelBuilderFactory.createLabel("engineType", "hive-1.2.1");
        System.out.println(engineType.getFeature());

        AliasServiceInstanceLabel emInstanceLabel = labelBuilderFactory.createLabel(AliasServiceInstanceLabel.class);
        emInstanceLabel.setAlias("hello");
        System.out.println(emInstanceLabel.getStringValue());
    }
}
