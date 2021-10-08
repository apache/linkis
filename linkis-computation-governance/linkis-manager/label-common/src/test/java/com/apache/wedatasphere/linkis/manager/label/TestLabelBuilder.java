package com.apache.wedatasphere.linkis.manager.label;

import com.apache.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactory;
import com.apache.wedatasphere.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import com.apache.wedatasphere.linkis.manager.label.builder.factory.StdLabelBuilderFactory;
import com.apache.wedatasphere.linkis.manager.label.entity.Label;
import com.apache.wedatasphere.linkis.manager.label.entity.node.AliasServiceInstanceLabel;
import com.apache.wedatasphere.linkis.manager.label.exception.LabelErrorException;


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
