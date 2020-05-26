package com.webank.wedatasphere.linkis.cs.optimize.dfs;

public interface Node {
    Double getCost();
    Double getPriority();
    Node getLeft();
    Node getRight();
    void shift();
    boolean visited();
    void visit();
}
