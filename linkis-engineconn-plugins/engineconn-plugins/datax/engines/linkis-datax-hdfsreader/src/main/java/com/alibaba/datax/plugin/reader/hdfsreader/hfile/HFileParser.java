package com.alibaba.datax.plugin.reader.hdfsreader.hfile;

import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.plugin.reader.hdfsreader.Key;

/**
 * Parser
 * @author davidhua
 * 2020/2/24
 */
public interface HFileParser {
    String[] PARSE_COLUMNS = new String[]{Key.HFILE_PARSE_ROW_KEY,
            Key.HFILE_PARSE_FAMILY, Key.HFIEL_PARSE_QUALIFIER,
            Key.HFILE_PARSE_VALUE, Key.HFILE_TIMESTAMP};
    /**
     * Parse HFile under input path
     * @param inputPath input path
     * @param parseConf configuration for parsing
     * @param action action
     */
    void parse(String inputPath, Configuration parseConf, Action action);

    @FunctionalInterface
    interface Action{
        /**
         * Process source lines
         * @param sourceLine source line
         * @return
         */
        void process(String[] sourceLine);
    }
}
