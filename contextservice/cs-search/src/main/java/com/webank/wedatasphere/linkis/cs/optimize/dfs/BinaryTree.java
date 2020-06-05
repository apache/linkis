package com.webank.wedatasphere.linkis.cs.optimize.dfs;

public interface BinaryTree {
    BinaryTree getLeft();
    BinaryTree getRight();
    Node getRootNode();
    Double getCost();
}
