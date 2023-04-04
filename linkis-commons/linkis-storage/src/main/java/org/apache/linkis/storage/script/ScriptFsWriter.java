package org.apache.linkis.storage.script;

import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.common.io.FsWriter;
import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.storage.LineRecord;
import org.apache.linkis.storage.script.compaction.PYScriptCompaction;
import org.apache.linkis.storage.script.compaction.QLScriptCompaction;
import org.apache.linkis.storage.script.compaction.ScalaScriptCompaction;
import org.apache.linkis.storage.script.compaction.ShellScriptCompaction;
import org.apache.linkis.storage.script.parser.PYScriptParser;
import org.apache.linkis.storage.script.parser.QLScriptParser;
import org.apache.linkis.storage.script.parser.ScalaScriptParser;
import org.apache.linkis.storage.script.parser.ShellScriptParser;
import org.apache.linkis.storage.script.writer.StorageScriptFsWriter;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public abstract class ScriptFsWriter extends FsWriter {
    FsPath path;
    String charset;

    public abstract InputStream getInputStream();

    public static ScriptFsWriter getScriptFsWriter(
             FsPath path,
             String charset ,
             OutputStream outputStream) {
        return new StorageScriptFsWriter(path, charset, outputStream);
    }

}

