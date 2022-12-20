package org.apache.linkis.basedatamanager.server.restful;

import org.apache.linkis.basedatamanager.server.Scan;
import org.apache.linkis.basedatamanager.server.WebApplicationServer;
import org.apache.linkis.basedatamanager.server.service.DatasourceAccessService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.MessageStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SpringExtension.class})
@AutoConfigureMockMvc
@SpringBootTest(classes = {WebApplicationServer.class, Scan.class})
public class DatasourceAccessRestfulApiTest {
    private Logger logger = LoggerFactory.getLogger(DatasourceAccessRestfulApiTest.class);

    @Autowired protected MockMvc mockMvc;

    @Mock
    private DatasourceAccessService datasourceAccessService;

    @Test
    public void TestGetListByPage() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("searchName", "");
        paramsMap.add("currentPage", "1");
        paramsMap.add("pageSize", "10");
        String url = "/basedata-manager/datasource-access";
        sendUrl(url, paramsMap, "get", null);
    }
    public void sendUrl(String url, MultiValueMap<String, String> paramsMap, String type, String msg)
            throws Exception {
        MvcUtils mvcUtils = new MvcUtils(mockMvc);
        Message mvcResult = null;
        if (type.equals("get")) {
            if (paramsMap != null) {
                mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url, paramsMap));
            } else {
                mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultGet(url));
            }
        }
        if (type.equals("post")) {
            if (msg != null) {
                mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url, msg));
            } else {
                mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url));
            }
        }
        assertEquals(MessageStatus.SUCCESS(), mvcResult.getStatus());
        logger.info(String.valueOf(mvcResult));
    }


}
