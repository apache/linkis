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
 
package org.apache.linkis.bml.threading;

public enum TaskState {
    /**
     * 任务的状态的内容
     */
    INITED("inited", 0),
    SCHEDULED("scheduled", 1),
    RUNNING("running", 2),
    SUCCESS("success",3),
    FAILED("failed", 4),
    CANCELLED("cancelled", 5);
    private String value;
    private int id;
    private TaskState(String value, int id){
        this.value = value;
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static boolean isCompleted(TaskState state){
        return state.equals(SUCCESS) || state.equals(FAILED) || state.equals(CANCELLED);
    }

    public static boolean isCompleted(String state){
        return SUCCESS.getValue().equals(state) || FAILED.getValue().equals(state) || CANCELLED.getValue().equals(state);
    }



}
