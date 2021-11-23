package com.alibaba.datax.common.plugin;

/**
 * @author davidhua
 * 2019/8/26
 */
public interface PluginProcessorLoader {

    /**
     * load
     * @param fullClassName
     * @param javaCode
     * @param classpath
     * @return
     */
    boolean load(String fullClassName, String javaCode, String classpath);

    /**
     * load
     * @param fullClassName
     * @param javaCode
     * return
     */
    boolean load(String fullClassName, String javaCode);
}
