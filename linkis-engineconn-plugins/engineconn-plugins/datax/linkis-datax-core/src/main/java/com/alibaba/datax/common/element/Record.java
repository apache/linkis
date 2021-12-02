package com.alibaba.datax.common.element;


import org.apache.linkis.datax.core.transport.channel.ChannelElement;

import java.util.List;

/**
 * Created by jingxing on 14-8-24.
 */

public interface Record extends ChannelElement {

    void addColumn(Column column);

    void setColumn(int i, final Column column);

    Column getColumn(int i);

    @Override
    String toString();

    int getColumnNumber();

    List<Column> getColumns();

    /**
     * Unique id
     * @return
     */
    String uid();
}
