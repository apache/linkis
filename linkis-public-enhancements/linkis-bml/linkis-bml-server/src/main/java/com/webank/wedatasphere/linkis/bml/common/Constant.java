/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.bml.common;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cooperyang on 2019/5/21.
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
