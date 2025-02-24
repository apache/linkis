/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.filesystem.restful.api;

import org.apache.linkis.filesystem.Scan;
import org.apache.linkis.filesystem.WebApplicationServer;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {WebApplicationServer.class, Scan.class})
@AutoConfigureMockMvc
public class FsRestfulApiTest {
  //
  //  private static final Logger LOG = LoggerFactory.getLogger(FsRestfulApiTest.class);
  //
  //  @InjectMocks private FsRestfulApi fsRestfulApi;
  //
  //  @Autowired private MockMvc mockMvc;
  //
  //  @MockBean(name = "fsService")
  //  private FsService fsService;
  //
  //  @Test
  //  @DisplayName("getDirFileTreesTest")
  //  public void getDirFileTreesTest() throws Exception {
  //
  //    if (!FsPath.WINDOWS) {
  //      FileSystem fs = new LocalFileSystem();
  //      fs.setUser("docker");
  //      String group =
  //          Files.readAttributes(
  //                  Paths.get(this.getClass().getResource("/").getPath()),
  // PosixFileAttributes.class)
  //              .group()
  //              .getName();
  //      fs.setGroup(new FsPath(this.getClass().getResource("/").getPath()), group);
  //
  //      Mockito.when(fsService.getFileSystem(Mockito.anyString(), Mockito.any(FsPath.class)))
  //          .thenReturn(fs);
  //      String path = this.getClass().getResource("/").getPath();
  //
  //      MvcResult mvcResult =
  //          mockMvc
  //              .perform(get("/filesystem/getDirFileTrees").param("path", path))
  //              .andExpect(status().isOk())
  //              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
  //              .andReturn();
  //
  //      Message res =
  //          JsonUtils.jackson()
  //              .readValue(mvcResult.getResponse().getContentAsString(), Message.class);
  //
  //      assertEquals(MessageStatus.SUCCESS(), res.getStatus());
  //      LOG.info(mvcResult.getResponse().getContentAsString());
  //    }
  //  }
  //
  //  @Test
  //  @DisplayName("isExistTest")
  //  public void isExistTest() throws Exception {
  //
  //    FileSystem fs = new LocalFileSystem();
  //    fs.setUser("docker");
  //    Mockito.when(fsService.getFileSystem(Mockito.anyString(), Mockito.any(FsPath.class)))
  //        .thenReturn(fs);
  //    String path = this.getClass().getResource("/").getPath();
  //
  //    MvcResult mvcResult =
  //        mockMvc
  //            .perform(get("/filesystem/isExist").param("path", path))
  //            .andExpect(status().isOk())
  //            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
  //            .andReturn();
  //
  //    Message res =
  //        JsonUtils.jackson().readValue(mvcResult.getResponse().getContentAsString(),
  // Message.class);
  //
  //    assertEquals(MessageStatus.SUCCESS(), res.getStatus());
  //    LOG.info(mvcResult.getResponse().getContentAsString());
  //  }
  //
  //  @Test
  //  @DisplayName("fileInfoTest")
  //  public void fileInfoTest() throws Exception {
  //    if (!FsPath.WINDOWS) {
  //      FileSystem fs = new LocalFileSystem();
  //      fs.setUser("docker");
  //      String group =
  //          Files.readAttributes(
  //                  Paths.get(this.getClass().getResource("/").getPath()),
  // PosixFileAttributes.class)
  //              .group()
  //              .getName();
  //      fs.setGroup(new FsPath(this.getClass().getResource("/").getPath()), group);
  //      Mockito.when(fsService.getFileSystem(Mockito.anyString(), Mockito.any(FsPath.class)))
  //          .thenReturn(fs);
  //      String path = this.getClass().getResource("/").getPath() + "query.sql";
  //
  //      MvcResult mvcResult =
  //          mockMvc
  //              .perform(get("/filesystem/fileInfo").param("path", path))
  //              .andExpect(status().isOk())
  //              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
  //              .andReturn();
  //
  //      Message res =
  //          JsonUtils.jackson()
  //              .readValue(mvcResult.getResponse().getContentAsString(), Message.class);
  //
  //      assertEquals(MessageStatus.SUCCESS(), res.getStatus());
  //      LOG.info(mvcResult.getResponse().getContentAsString());
  //    }
  //  }
  //
  //  @Test
  //  @DisplayName("openFileTest")
  //  public void openFileTest() throws Exception {
  //
  //    if (!FsPath.WINDOWS) {
  //      FileSystem fs = new LocalFileSystem();
  //      fs.setUser("docker");
  //      String group =
  //          Files.readAttributes(
  //                  Paths.get(this.getClass().getResource("/").getPath()),
  // PosixFileAttributes.class)
  //              .group()
  //              .getName();
  //      fs.setGroup(new FsPath(this.getClass().getResource("/").getPath()), group);
  //
  //      Mockito.when(fsService.getFileSystem(Mockito.anyString(), Mockito.any(FsPath.class)))
  //          .thenReturn(fs);
  //      String path = this.getClass().getResource("/").getPath() + "query.sql";
  //
  //      MvcResult mvcResult =
  //          mockMvc
  //              .perform(get("/filesystem/fileInfo").param("path", path))
  //              .andExpect(status().isOk())
  //              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
  //              .andReturn();
  //
  //      Message res =
  //          JsonUtils.jackson()
  //              .readValue(mvcResult.getResponse().getContentAsString(), Message.class);
  //
  //      assertEquals(MessageStatus.SUCCESS(), res.getStatus());
  //      LOG.info(mvcResult.getResponse().getContentAsString());
  //    }
  //  }
  //
  //  @Test
  //  @DisplayName("openLogTest")
  //  public void openLogTest() throws Exception {
  //
  //    if (!FsPath.WINDOWS) {
  //      FileSystem fs = new LocalFileSystem();
  //      fs.setUser("docker");
  //      String group =
  //          Files.readAttributes(
  //                  Paths.get(this.getClass().getResource("/").getPath()),
  // PosixFileAttributes.class)
  //              .group()
  //              .getName();
  //      fs.setGroup(new FsPath(this.getClass().getResource("/").getPath()), group);
  //
  //      Mockito.when(fsService.getFileSystem(Mockito.anyString(), Mockito.any(FsPath.class)))
  //          .thenReturn(fs);
  //      String path = this.getClass().getResource("/").getPath() + "info.log";
  //
  //      MvcResult mvcResult =
  //          mockMvc
  //              .perform(get("/filesystem/openLog").param("path", path))
  //              .andExpect(status().isOk())
  //              .andExpect(content().contentType(MediaType.APPLICATION_JSON))
  //              .andReturn();
  //
  //      Message res =
  //          JsonUtils.jackson()
  //              .readValue(mvcResult.getResponse().getContentAsString(), Message.class);
  //
  //      assertEquals(MessageStatus.SUCCESS(), res.getStatus());
  //      LOG.info(mvcResult.getResponse().getContentAsString());
  //    }
  //  }
}
