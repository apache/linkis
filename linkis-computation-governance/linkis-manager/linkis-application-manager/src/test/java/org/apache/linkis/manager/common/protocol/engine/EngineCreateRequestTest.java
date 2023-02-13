package org.apache.linkis.manager.common.protocol.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngineCreateRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testTimeout() {
        ObjectNode jNode = objectMapper.createObjectNode();
       // "timeout", "properties", "createService", "user", "description", "labels", "ignoreTimeout"
        jNode.put("timeout",1000);
        jNode.put("user","hadoop");
        jNode.put("description","test for node");
        jNode.put("ignoreTimeout",false);

        try {
            EngineCreateRequest engineCreateRequest =
                    objectMapper.treeToValue(jNode, EngineCreateRequest.class);
             assertEquals(engineCreateRequest.getTimeout(),jNode.get("timeout").asLong());
        } catch (JsonProcessingException e) {
            fail("Should not have thrown any exception",e);
        }
    }


    @Test
    void testTimeOut() {
        ObjectNode jNode = objectMapper.createObjectNode();
        // "timeout", "properties", "createService", "user", "description", "labels", "ignoreTimeout"
        jNode.put("timeOut",1000);
        jNode.put("user","hadoop");
        jNode.put("description","test for node");
        jNode.put("ignoreTimeout",false);

        try {
            EngineCreateRequest engineCreateRequest =
                    objectMapper.treeToValue(jNode, EngineCreateRequest.class);
            assertEquals(engineCreateRequest.getTimeout(),jNode.get("timeOut").asLong());
        } catch (JsonProcessingException e) {
            fail("Should not have thrown any exception",e);
        }
    }
}