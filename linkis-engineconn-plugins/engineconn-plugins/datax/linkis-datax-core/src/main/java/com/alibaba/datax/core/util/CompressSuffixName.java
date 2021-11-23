package com.alibaba.datax.core.util;


import org.apache.commons.lang3.StringUtils;

/**
 * @author davidhua
 * 2019/5/30
 */
public enum CompressSuffixName {
    //Compress list
    LZO_DEFLAT("lzo_deflat", ".lzo_deflat"),
    LZO("lzo", ".lzo"),
    GZIP("gzip", ".gz"),
    BZIP2("bzip2", ".bz2"),
    HADOOP_SNAPPY("hadoop-snappy", ".snappy"),
    SNAPPY("snappy", ".snappy"),
    FRAMING_SNAPPY("framing-snappy", ".snappy"),
    ZIP("zip", ".zip"),
    NONE("none", "");

    private String compressName;

    private String suffix;

     CompressSuffixName(String compressName, String suffix){
        this.compressName = compressName;
        this.suffix = suffix;
    }

    public static String chooseSuffix(String compressName){
         if(StringUtils.isBlank(compressName)){
             return null;
         }
         CompressSuffixName compressSuffixName =
                 CompressSuffixName.valueOf(compressName.toUpperCase());
         return compressSuffixName.getSuffix();
    }
    public String getSuffix(){
        return suffix;
    }

    public String getCompressName(){
        return compressName;
    }
}
