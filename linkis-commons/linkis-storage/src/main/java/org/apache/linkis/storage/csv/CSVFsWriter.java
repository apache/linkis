package org.apache.linkis.storage.csv;

import org.apache.linkis.common.io.FsWriter;
import java.io.OutputStream;

public abstract class CSVFsWriter extends FsWriter {
    public abstract String getCharset();
    public abstract String getSeparator();
    public abstract boolean isQuoteRetouchEnable();

    public static CSVFsWriter getCSVFSWriter(String charset, String separator, boolean quoteRetouchEnable, OutputStream outputStream) {
        return new StorageCSVWriter(charset, separator, quoteRetouchEnable, outputStream);
    }
}
