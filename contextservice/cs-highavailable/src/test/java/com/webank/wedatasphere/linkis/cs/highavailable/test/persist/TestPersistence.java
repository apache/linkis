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
package com.webank.wedatasphere.linkis.cs.highavailable.test.persist;

import com.google.gson.Gson;
import com.webank.wedatasphere.linkis.cs.common.entity.source.HAContextID;

public class TestPersistence {

    private Gson gson = new Gson();

    public void testPrint() {
        System.out.println("TestPersistence: testPrint()");
    }

    public HAContextID createHAID(HAContextID haContextID) {
        System.out.println("TestPersistence: createHAID(), params: haContextID : " + gson.toJson(haContextID));
        haContextID.setContextId("1");
        return haContextID;
    }

    public HAContextID passHAID(HAContextID haContextID) {
        System.out.println("TestPersistence: passHAID(), params: haContextID : " + gson.toJson(haContextID));
        return haContextID;
    }

    public void setContextId(String haid) {
        System.out.println("TestPersistence: setContextId(), : " + gson.toJson(haid));
    }


}
