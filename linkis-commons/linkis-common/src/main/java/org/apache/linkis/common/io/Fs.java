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
 
package org.apache.linkis.common.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;


public interface Fs extends Closeable {

    public abstract void init(Map<String, String> properties) throws IOException;

    public abstract String fsName();

    String rootUserName();

    FsPath get(String dest) throws IOException;

    public abstract InputStream read(FsPath dest) throws IOException;

    public abstract OutputStream write(FsPath dest, boolean overwrite) throws IOException;

    boolean create(String dest) throws IOException;

    List<FsPath> list(final FsPath path) throws IOException;

    public abstract boolean canRead(FsPath dest) throws IOException;

    public abstract boolean canWrite(FsPath dest) throws IOException;

    public abstract boolean exists(FsPath dest) throws IOException;

    public abstract boolean delete(FsPath dest) throws IOException;

    public abstract boolean renameTo(FsPath oldDest, FsPath newDest) throws IOException;

    public abstract boolean mkdir(FsPath dest) throws IOException;

    public abstract boolean mkdirs(FsPath dest) throws IOException;

}
