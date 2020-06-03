package com.webank.wedatasphere.linkis.cs.client.test.test_multiuser;

import com.google.gson.Gson;
import com.webank.wedatasphere.linkis.cs.client.Context;
import com.webank.wedatasphere.linkis.cs.client.ContextClient;
import com.webank.wedatasphere.linkis.cs.client.builder.ContextClientFactory;
import com.webank.wedatasphere.linkis.cs.client.service.DefaultSearchService;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.resource.BMLResource;
import com.webank.wedatasphere.linkis.cs.common.entity.resource.LinkisBMLResource;
import com.webank.wedatasphere.linkis.cs.common.entity.source.*;
import com.webank.wedatasphere.linkis.cs.common.serialize.helper.ContextSerializationHelper;
import com.webank.wedatasphere.linkis.cs.common.serialize.helper.SerializationHelper;
import scala.tools.nsc.doc.model.Def;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author alexyang
 * @Date 2020/2/28
 */
public class TestCreateContext {

    public static final String CONTEXTID_PATH = "out/test-store-contextID.txt";

    public static void main(String [] args) throws Exception {

        // 1, create contextid
        ContextClient contextClient = ContextClientFactory.getOrCreateContextClient();
        try {

            Context context = contextClient.createContext("test_client", "test_client", "alex", null);
            System.out.println(context.getContextID().getContextId());

            // 2, save contxtid
            ContextID contextID = context.getContextID();
            SerializationHelper serializationHelper = ContextSerializationHelper.getInstance();
            String contextIDStr = null;
            if (serializationHelper.accepts(contextID)) {
                contextIDStr = serializationHelper.serialize(contextID);
                File file = new File(CONTEXTID_PATH);
                FileWriter fr = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fr);
                bw.write(contextIDStr);
                bw.flush();
                bw.close();
                System.out.println("ContextID saved at : " + file.getAbsolutePath());
                // test deserialize
                System.out.println("contextIDStr: " + contextIDStr);
                Object obj = serializationHelper.deserialize(contextIDStr);
                System.out.println("Deserialize jsonstr : " + new Gson().toJson(obj));
            } else {
                System.out.println("ContextID not saved.");
            }

            // 3, test search
            ContextKey contextKey = new CommonContextKey();
            contextKey.setKey("cooper.txt");
            contextKey.setKeywords("xddd");
            contextKey.setContextScope(ContextScope.PUBLIC);
            contextKey.setContextType(ContextType.RESOURCE);
            ContextValue contextValue = new CommonContextValue();
            LinkisBMLResource resource = new LinkisBMLResource();
            resource.setResourceId("456789");
            resource.setVersion("v00001");
            contextValue.setValue(resource);
            ContextKeyValue contextKeyValue = new CommonContextKeyValue();
            contextKeyValue.setContextValue(contextValue);
            contextKeyValue.setContextKey(contextKey);
            context.setContextKeyAndValue(contextKeyValue);
            ContextValue myValue = context.getContextValue(contextKey);
            LinkisBMLResource linkisBMLResource = (LinkisBMLResource)myValue.getValue();
            System.out.println(linkisBMLResource.getResourceId());
            BMLResource rs = DefaultSearchService.getInstance().getContextValue(contextID, contextKey, LinkisBMLResource.class);
            System.out.println(new Gson().toJson(rs));
            List<String>  contains = new ArrayList<>();
            contains.add("cooper");
            List<ContextKeyValue> contextKeyValueList = contextClient.search(context.getContextID(), null, null, contains, null);
            System.out.println("ContextKVList : " + new Gson().toJson(contextKeyValueList));
        } catch (Exception e) {
            contextClient.close();
            e.printStackTrace();
        }

    }
}
