package com.webank.wedatasphere.linkis.engine.impala.client.protocol;

/**
 * 查询的列名
 * 
 * @author dingqihuang
 * @version Sep 20, 2019
 */
public class QueryColumn {
    private String label;
    private int index;
    
    /**
     * @param label
     * @param index
     */
    public QueryColumn(String label, int index) {
        super();
        this.label = label;
        this.index = index;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    
}
