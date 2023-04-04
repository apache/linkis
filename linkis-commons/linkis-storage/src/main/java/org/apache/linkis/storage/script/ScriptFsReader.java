package org.apache.linkis.storage.script;

import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.common.io.FsReader;
import org.apache.linkis.storage.script.reader.StorageScriptFsReader;

import java.io.InputStream;

public abstract class ScriptFsReader extends FsReader {

    protected FsPath path;
    protected String charset;

    public ScriptFsReader(FsPath path, String charset) {
        this.path = path;
        this.charset = charset;
    }

    public static ScriptFsReader getScriptFsReader(FsPath path, String charset, InputStream inputStream) {
        return new StorageScriptFsReader(path, charset, inputStream);
    }

}