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

package org.apache.linkis.metadata.restful.remote;

import org.apache.linkis.metadata.util.Constants;
import org.apache.linkis.server.Message;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@FeignClient(name = Constants.APPLICATION_NAME)
public interface DataSourceRestfulRemote {

  @GetMapping("/api/datasource/dbs")
  public Message queryDatabaseInfo(HttpServletRequest req, String permission);

  @GetMapping("/api/datasource/all")
  public Message queryDbsWithTables(HttpServletRequest req);

  @GetMapping("/api/datasource/getByAccessTime")
  public Message queryDbsWithTablesgetByAccessTime(HttpServletRequest req);

  @GetMapping("/api/datasource/tables")
  public Message queryTables(@RequestParam("database") String database, HttpServletRequest req);

  @GetMapping("/api/datasource/columns")
  public Message queryTableMeta(
      @RequestParam("database") String database,
      @RequestParam("table") String table,
      HttpServletRequest req);

  @GetMapping("/api/datasource/size")
  public Message sizeOf(
      @RequestParam("database") String database,
      @RequestParam("table") String table,
      @RequestParam("partition") String partition,
      HttpServletRequest req);

  @GetMapping("/api/datasource/partitions")
  public Message partitions(
      @RequestParam("database") String database,
      @RequestParam("table") String table,
      HttpServletRequest req);
}
