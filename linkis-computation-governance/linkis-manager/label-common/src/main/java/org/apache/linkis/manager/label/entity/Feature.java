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
 
package org.apache.linkis.manager.label.entity;

/**
 * Identify the characters of label
 */
public enum Feature {
    /**
     * Key Feature
     */
    CORE(2.0f, true),
    /**
     * Suitable Feature
     */
    SUITABLE(1.0f, true),
    /**
     * Priority Feature
     */
    PRIORITIZED(0.8f, false),
    /**
     * Option Feature/Addition Feature
     */
    OPTIONAL(0.5f, false),
    /**
     * UNKNOWN
     */
    UNKNOWN(0.3f, false);
    /**
     * Boost value, used in scoring function
     */
    private float boost;

    /**
     * If the feature is necessary when scoring
     */
    private boolean necessary;

    Feature(float boost, boolean necessary){
        this.boost = boost;
        this.necessary = necessary;
    }

    public float getBoost(){
        return boost;
    }

    public boolean isNecessary(){
        return necessary;
    }
}
