package org.apache.linkis.storage.script;

import org.apache.linkis.storage.script.compaction.PYScriptCompaction;
import org.apache.linkis.storage.script.compaction.QLScriptCompaction;
import org.apache.linkis.storage.script.compaction.ScalaScriptCompaction;
import org.apache.linkis.storage.script.compaction.ShellScriptCompaction;

public interface Compaction {
    String prefixConf();

    String prefix();

    boolean belongTo(String suffix);

    String compact(Variable variable);

    public static Compaction[] listCompactions() {
        return new Compaction[]{
                new PYScriptCompaction(),
                new QLScriptCompaction(),
                new ScalaScriptCompaction(),
                new ShellScriptCompaction()
        };
    }

    String getAnnotationSymbol();
}