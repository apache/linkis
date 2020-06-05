package com.webank.wedatasphere.linkis.cs.optimize;

import com.webank.wedatasphere.linkis.cs.condition.BinaryLogicCondition;
import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.condition.UnaryLogicCondition;
import com.webank.wedatasphere.linkis.cs.optimize.cost.ConditionCostCalculator;
import com.webank.wedatasphere.linkis.cs.optimize.dfs.Node;

public class OptimizedCondition implements Node {

    static Double HIGH_PRIORITY = 1d;
    static Double LOW_PRIORITY = 0.5d;

    Condition condition;
    Double cost;
    Double priority;
    boolean visited = false;
    OptimizedCondition left;
    OptimizedCondition right;

    public OptimizedCondition(Condition condition, ConditionCostCalculator conditionCostCalculator) {
        new OptimizedCondition(condition, HIGH_PRIORITY, conditionCostCalculator);
    }

    public OptimizedCondition(Condition condition, Double priority, ConditionCostCalculator conditionCostCalculator) {
        this.condition = condition;
        this.priority = priority;
        this.cost = conditionCostCalculator.calculate(condition);
        if(condition instanceof BinaryLogicCondition){
            BinaryLogicCondition binaryLogicCondition = (BinaryLogicCondition) condition;
            this.left = new OptimizedCondition(binaryLogicCondition.getLeft(), HIGH_PRIORITY, conditionCostCalculator);
            this.right = new OptimizedCondition(binaryLogicCondition.getRight(), LOW_PRIORITY, conditionCostCalculator);
        } else if(condition instanceof UnaryLogicCondition){
            this.left = new OptimizedCondition(((UnaryLogicCondition) condition).getOrigin(), conditionCostCalculator);
        }
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    public Double getCost() {
        return this.cost;
    }

    @Override
    public Double getPriority() {
        return this.priority;
    }

    @Override
    public Node getLeft() {
        return this.left;
    }

    @Override
    public Node getRight() {
        return this.right;
    }

    @Override
    public void shift() {
        if(condition instanceof BinaryLogicCondition){
            OptimizedCondition tmp = this.left;
            this.left = this.right;
            this.right = tmp;
            this.right.priority = this.left.priority;
            this.left.priority = tmp.priority;
            ((BinaryLogicCondition) condition).shift();
        }
    }

    @Override
    public boolean visited() {
        return visited;
    }

    @Override
    public void visit() {
        this.visited = true;
    }
}
