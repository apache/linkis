/*
 *
 *  Copyright 2020 WeBank
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.datax.common.constant;

/**
 * @author davidhua
 * 2019/3/31
 */
public enum TransportType {
    // Used to dive the channel into two types
    RECORD("record"), STREAM("stream");
    private String transportType;
    TransportType(String transportType){
        this.transportType = transportType;
    }
    @Override
    public String toString(){
        return this.transportType;
    }
}
