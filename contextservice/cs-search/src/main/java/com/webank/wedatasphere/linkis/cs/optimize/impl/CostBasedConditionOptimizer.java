/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
