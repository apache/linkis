package org.apache.linkis.storage.script;

import org.apache.linkis.storage.script.parser.PYScriptParser;
import org.apache.linkis.storage.script.parser.QLScriptParser;
import org.apache.linkis.storage.script.parser.ScalaScriptParser;
import org.apache.linkis.storage.script.parser.ShellScriptParser;

public class ParserFactory {
    public static Parser[] listParsers() {
        return new Parser[]{
                new PYScriptParser(),
                new QLScriptParser(),
                new ScalaScriptParser(),
                new ShellScriptParser()
        };
    }
}