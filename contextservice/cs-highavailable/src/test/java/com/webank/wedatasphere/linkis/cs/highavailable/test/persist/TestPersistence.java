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
