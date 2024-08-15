package org.apache.linkis.cs.conf;

import org.apache.linkis.common.conf.CommonVars;

public class CSConfiguration {
    public static final CommonVars<String> CONTEXT_VALUE_TYPE_PREFIX_WHITE_LIST =
            CommonVars.apply("linkis.context.value.type.prefix.whitelist", "org.apache.linkis");

    public static final CommonVars<Boolean> ENABLE_CONTEXT_VALUE_TYPE_PREFIX_WHITE_LIST_CHECK =
            CommonVars.apply("linkis.context.value.type.prefix.whitelist.check.enable", true);
}
