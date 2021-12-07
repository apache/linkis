package com.alibaba.datax.core.transport.record;

import com.alibaba.datax.common.element.Column;
import com.alibaba.datax.common.element.Record;

import java.util.ArrayList;
import java.util.List;

/**
 * 作为标示 生产者已经完成生产的标志
 */
public class TerminateRecord implements Record {
    private final static TerminateRecord SINGLE = new TerminateRecord();

    private TerminateRecord() {
    }

    public static TerminateRecord get() {
        return SINGLE;
    }

    @Override
    public void addColumn(Column column) {
    }

    @Override
    public Column getColumn(int i) {
        return null;
    }

    @Override
    public int getColumnNumber() {
        return 0;
    }

    @Override
    public List<Column> getColumns() {
        return new ArrayList<>();
    }

    @Override
    public String uid() {
        return "";
    }

    @Override
    public int getByteSize() {
        return 0;
    }

    @Override
    public int getMemorySize() {
        return 0;
    }

    @Override
    public <T>T copyElement() {
        return (T)this;
    }

    @Override
    public void setColumn(int i, Column column) {
        return;
    }

}
