package org.apache.linkis.storage.script;

public class Variable {
    protected String sortParent;
    protected String sort;
    protected String key;
    protected String value;

    public Variable(String sortParent, String sort, String key, String value) {
        this.sortParent = sortParent;
        this.sort = sort;
        this.key = key;
        this.value = value;
    }

    public String getSortParent() {
        return sortParent;
    }

    public String getSort() {
        return sort;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

}