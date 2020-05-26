package com.webank.wedatasphere.linkis.cs.optimize.impl;

import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.optimize.ConditionOptimizer;
import com.webank.wedatasphere.linkis.cs.optimize.OptimizedCondition;
import com.webank.wedatasphere.linkis.cs.optimize.cost.ConditionCostCalculator;
import com.webank.wedatasphere.linkis.cs.optimize.dfs.MinCostBinaryTree;

public class CostBasedConditionOptimizer implements ConditionOptimizer {

    ConditionCostCalculator conditionCostCalculator;

    public CostBasedConditionOptimizer(ConditionCostCalculator conditionCostCalculator) {
        this.conditionCostCalculator = conditionCostCalculator;
    }

    @Override
    public OptimizedCondition optimize(Condition condition) {
        OptimizedCondition dfsTreeNode = new OptimizedCondition(condition, conditionCostCalculator);
        MinCostBinaryTree minCostBinaryTree = new MinCostBinaryTree(dfsTreeNode);
        return dfsTreeNode;
    }

}
