/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.protocol.label;

import org.apache.linkis.protocol.util.ImmutablePair;

import java.util.List;
import java.util.Map;


public class InsLabelQueryResponse {

    public InsLabelQueryResponse() {}

    public InsLabelQueryResponse(List<ImmutablePair<String, String>> labelList) {
        this.labelList = labelList;
    }

    private List<ImmutablePair<String, String>> labelList;

    public List<ImmutablePair<String, String>> getLabelList() {
        return labelList;
    }

    public InsLabelQueryResponse setLabelList(List<ImmutablePair<String, String>> labelList) {
        this.labelList = labelList;
        return this;
    }

}
