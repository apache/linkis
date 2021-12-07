package com.alibaba.datax.plugin.reader.hdfsreader.hfile;

import org.apache.hadoop.fs.FileSystem;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author davidhua
 * 2020/2/24
 */
public class HFileParserFactory {
    private static final ConcurrentHashMap<String, HFileParser> hFileParserStoreMap = new ConcurrentHashMap<>();

    /**
     * Get parser implemented by HBASE server
     * @param fileSystem
     * @return
     */
    public static HFileParser getHBASEImpl(FileSystem fileSystem){
        return hFileParserStoreMap.computeIfAbsent("HBASEImpl", key -> new HBASEV1HFileParser(fileSystem));
    }

    public static HFileParser getHBASEImpl(FileSystem fileSystem, String encoding){
        return hFileParserStoreMap.computeIfAbsent("HBASEImpl", key -> new HBASEV1HFileParser(fileSystem, encoding));
    }
}
