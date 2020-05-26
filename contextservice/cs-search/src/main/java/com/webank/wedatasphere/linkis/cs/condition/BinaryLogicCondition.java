package com.webank.wedatasphere.linkis.cs.condition;

public abstract class BinaryLogicCondition extends AbstractCommonCondition{

    Condition left;
    Condition right;

    public BinaryLogicCondition(Condition left, Condition right) {
        this.left = left;
        this.right = right;
    }

    public void shift(){
        Condition tmp = this.left;
        this.left = this.right;
        this.right = tmp;
    }

    public Condition getLeft() {
        return left;
    }

    public void setLeft(Condition left) {
        this.left = left;
    }

    public Condition getRight() {
        return right;
    }

    public void setRight(Condition right) {
        right = right;
    }
}
