package org.apache.linkis.storage.source;

import org.apache.linkis.common.io.Fs;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.storage.FSFactory;
import org.apache.linkis.storage.csv.CSVFsWriter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

class ResultsetFileSourceTest {

    @Test
    public void testWriter() throws IOException {

        FsPath sourceFsPath = new FsPath("file://tmp/linkis/result.dolphin");
        FsPath destFsPath = new FsPath("file://tmp/linkis/B.csv");
        Fs sourceFs = FSFactory.getFs(sourceFsPath);
        sourceFs.init(null);
        Fs destFs = FSFactory.getFs(destFsPath);
        destFs.init(null);

        OutputStream outputStream  = destFs.write(destFsPath, true);

        CSVFsWriter cSVFsWriter = CSVFsWriter.getCSVFSWriter(
                "UTF-8", ",", false,
                outputStream);

        FileSource fileSource = FileSource.create(sourceFsPath, sourceFs);
        fileSource.addParams("nullValue", "NULL").write(cSVFsWriter);

    }
}