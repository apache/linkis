package org.apache.linkis.storage.domain;

public final class Column {
    public String columnName;
    public DataType dataType;
    public String comment;

    public Column(String columnName, DataType dataType, String comment) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.comment = comment;
    }

    public String getColumnName() {
        return columnName;
    }

    public DataType getDataType() {
        return dataType;
    }

    public String getComment() {
        return comment;
    }

    public Object[] toArray() {
        return new Object[]{columnName, dataType, comment};
    }

    @Override
    public String toString() {
        return "columnName:" + columnName + ",dataType:" + dataType + ",comment:" + comment;
    }
}