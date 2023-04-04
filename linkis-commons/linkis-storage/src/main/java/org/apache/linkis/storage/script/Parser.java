package org.apache.linkis.storage.script;

public interface Parser {
    String prefixConf();

    String prefix();

    boolean belongTo(String suffix);

    Variable parse(String line);

    String getAnnotationSymbol();
}

