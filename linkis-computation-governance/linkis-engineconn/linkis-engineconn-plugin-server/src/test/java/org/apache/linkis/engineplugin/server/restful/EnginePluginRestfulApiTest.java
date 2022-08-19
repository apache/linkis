package org.apache.linkis.engineplugin.server.restful;

import org.apache.linkis.common.utils.JsonUtils;
import org.apache.linkis.engineplugin.Scan;
import org.apache.linkis.engineplugin.WebApplicationServer;
import org.apache.linkis.engineplugin.server.conf.EngineConnPluginSpringConfiguration;
import org.apache.linkis.engineplugin.server.entity.EngineConnBmlResource;
import org.apache.linkis.engineplugin.server.service.EngineConnResourceService;
import org.apache.linkis.engineplugin.server.service.EnginePluginAdminService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.MessageStatus;
import org.apache.linkis.server.security.SecurityFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpServletRequest;

import java.io.StringWriter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;

@ExtendWith({SpringExtension.class})
@AutoConfigureMockMvc
@SpringBootTest(classes = {WebApplicationServer.class, Scan.class})
public class EnginePluginRestfulApiTest {
  @Autowired protected MockMvc mockMvc;

  @MockBean private EnginePluginAdminService enginePluginAdminService;
  @MockBean private EngineConnResourceService engineConnResourceService;
  @MockBean private EngineConnPluginSpringConfiguration engineConnPluginSpringConfiguration;

  private static MockedStatic<SecurityFilter> securityFilter;

  @BeforeAll
  private static void init() {
    securityFilter = Mockito.mockStatic(SecurityFilter.class);
  }

  @AfterAll
  private static void close() {
    securityFilter.close();
  }

  @Test
  void rollBackEnginePlugin() throws Exception {
    String bmlVersion = "afb49680-91dc-483e-a30f-1075fa973afb";
    String url = "/engineplugin/rollBack";
    MvcUtils mvcUtils = new MvcUtils(mockMvc);
    EngineConnBmlResource engineConnBmlResource = new EngineConnBmlResource();
    engineConnBmlResource.setBmlResourceVersion(bmlVersion);
    StringWriter dsJsonWriter = new StringWriter();
    JsonUtils.jackson().writeValue(dsJsonWriter, engineConnBmlResource);
    securityFilter
        .when(() -> SecurityFilter.getLoginUsername(isA(HttpServletRequest.class)))
        .thenReturn("testUser", "hadoop");
    Message mvcResult =
        mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url, dsJsonWriter.toString()));
    assertTrue(
        MessageStatus.ERROR() == mvcResult.getStatus()
            && mvcResult.getMessage().contains("Only administrators can operate"));

    mvcResult = mvcUtils.getMessage(mvcUtils.buildMvcResultPost(url, dsJsonWriter.toString()));

    Mockito.doNothing().when(enginePluginAdminService).rollBackEnginePlugin(any());
    assertTrue(
        MessageStatus.SUCCESS() == mvcResult.getStatus()
            && "afb49680-91dc-483e-a30f-1075fa973afb"
                .equals(mvcResult.getData().get("insertId").toString()));
  }
}
