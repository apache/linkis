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
package com.webank.wedatasphere.linkis.cs.common.serialize.helper;

import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;

/**
 * @author peacewong
 * @date 2020/2/27 19:03
 */
public interface SerializationHelper {

    boolean accepts(String json);

    boolean accepts(Object obj);

    String serialize(Object obj) throws CSErrorException;

    Object deserialize(String json) throws CSErrorException;

    <T> T deserialize(String s, Class<T> interfaceClass) throws CSErrorException;


}
