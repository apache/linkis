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

package org.apache.linkis.manager.engineplugin.hbase.shell;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HBaseShellCommands {
  private static final Logger LOG = LoggerFactory.getLogger(HBaseShellCommands.class);
  private static final String COMMANDS_PATH = "hbase-ruby/shell/commands/";
  private static volatile Set<String> commandsSet;

  private HBaseShellCommands() {}

  public static Set<String> getAllCommands() throws IOException {
    if (commandsSet == null) {
      synchronized (HBaseShellCommands.class) {
        if (commandsSet == null) {
          Set<String> sortedSet = new TreeSet<>();
          URL commandFilesUrl =
              HBaseShellCommands.class.getClassLoader().getResource(COMMANDS_PATH);
          if (commandFilesUrl == null) {
            throw new IOException("The command files path is null!");
          }
          String commandFilePath = commandFilesUrl.getPath();
          File commandFile = new File(commandFilePath);
          if (!commandFile.exists()) {
            LOG.warn("The command files path is not exists, starting read file from jar.");
            String jarPath =
                commandFilesUrl
                    .toString()
                    .substring(0, commandFilesUrl.toString().indexOf("!/") + 2);
            LOG.info("The path in jar is " + jarPath);
            URL jarUrl = new URL(jarPath);
            JarURLConnection jarCon = (JarURLConnection) jarUrl.openConnection();
            JarFile jarFile = jarCon.getJarFile();
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
              JarEntry entry = jarEntries.nextElement();
              String name = entry.getName();
              if (!entry.isDirectory() && name.startsWith(COMMANDS_PATH)) {
                String commandName =
                    name.substring(name.lastIndexOf(File.separator) + 1, name.lastIndexOf(".rb"));
                sortedSet.add(commandName);
              }
            }

          } else {
            String[] files = commandFile.list();
            if (files == null) {
              throw new IOException("The command files is null!");
            }
            for (String file : files) {
              if (file.endsWith(".rb")) {
                sortedSet.add(file.substring(0, file.lastIndexOf(".rb")));
              }
            }
          }

          commandsSet = sortedSet;
        }
      }
    }
    return commandsSet;
  }

  public static List<String> searchCommand(String subCommand) {
    List<String> matchCommands = new ArrayList<>();

    try {
      Set<String> allCommands = getAllCommands();
      for (String command : allCommands) {
        if (command.startsWith(subCommand)) {
          matchCommands.add(command);
        }
      }
    } catch (IOException e) {
      return matchCommands;
    }
    return matchCommands;
  }
}
