package com.webank.wedatasphere.linkis.bml.common;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

/**
 * Created by v_wbjjianli on 2019/5/21.
 */
public interface Constant {

    String FILE_SYSTEM_USER = "neiljianliu";

    List<String> resourceTypes = Lists.newArrayList(Arrays.asList("hdfs", "share", "file"));

    String FIRST_VERSION = "v000001";

    String DEFAULT_SYSTEM = "WTSS";

    String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    String VERSION_FORMAT = "%06d";

    String VERSION_PREFIX = "v";

}
