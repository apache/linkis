package com.webank.wedatasphere.linkis.cs.optimize.dfs;

public class MinCostBinaryTree implements BinaryTree{

    Node rootNode;
    BinaryTree left;
    BinaryTree right;
    Double cost;

    public MinCostBinaryTree(Node rootNode) {
        this.rootNode = rootNode;
        if(this.rootNode.visited()){
            return;
        }
        if(rootNode.getLeft() == null){
            this.cost = rootNode.getCost();
            return;
        }
        if(rootNode.getRight() == null){
            this.left = new MinCostBinaryTree(rootNode.getLeft());
            this.cost = this.left.getCost();
            return;
        }

        BinaryTree firstLeft = new MinCostBinaryTree(rootNode.getLeft());
        BinaryTree firstRight = new MinCostBinaryTree(rootNode.getRight());
        Double firstCost = firstLeft.getCost() * rootNode.getLeft().getPriority() + firstRight.getCost()*rootNode.getRight().getPriority();

        rootNode.shift();

        BinaryTree secondLeft = new MinCostBinaryTree(rootNode.getLeft());
        BinaryTree secondRight = new MinCostBinaryTree(rootNode.getRight());
        Double secondCost = secondLeft.getCost() * rootNode.getLeft().getPriority() + secondRight.getCost()*rootNode.getRight().getPriority();

        if(firstCost > secondCost){
            this.left = secondLeft;
            this.right = secondRight;
            this.cost = secondCost;
        } else {
            rootNode.shift();
            this.left = firstLeft;
            this.right = secondRight;
            this.cost = firstCost;
        }
        this.rootNode.visit();
    }

    @Override
    public BinaryTree getLeft() {
        return this.left;
    }

    @Override
    public BinaryTree getRight() {
        return this.right;
    }

    @Override
    public Node getRootNode() {
        return this.rootNode;
    }

    @Override
    public Double getCost() {
        return this.cost;
    }
}
