package org.apache.linkis.storage;

import org.apache.linkis.common.io.Record;
import org.apache.linkis.storage.resultset.ResultRecord;

public class LineRecord implements ResultRecord {
    public String line;

    public LineRecord(String line) {
        this.line = line;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Override
    public Record cloneRecord() {
        return new LineRecord(line);
    }

    @Override
    public String toString() {
        return line;
    }
}
