/*
 *
 *  Copyright 2020 WeBank
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.datax.core.transport.checkpoint.storage;

import org.apache.linkis.datax.core.transport.checkpoint.CheckPoint;
import org.apache.linkis.datax.core.transport.checkpoint.CheckPointStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author davidhua
 * 2020/3/19
 */
public class CheckPointStoreFile implements CheckPointStore {

    private Map<String, Long> checkPointOffset = new ConcurrentHashMap<>();

    private BufferedRandomAccessFile accessFile;

    private CheckPointStoreFile(File pathFile) throws FileNotFoundException {
        this.accessFile = new BufferedRandomAccessFile(pathFile, "rw");
    }
    public static CheckPointStoreFile load(String path){
        CheckPointStoreFile storeFile = null;
        File pathFile = new File(path);
        if(pathFile.exists()){
            try {
                storeFile = new CheckPointStoreFile(pathFile);
            } catch (FileNotFoundException e) {
                storeFile = null;
            }
        }
        return storeFile;
    }

    public static CheckPointStoreFile create(String path){
        try {
           return new CheckPointStoreFile(new File(path));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public void savePoint(CheckPoint checkPoint) {

    }

    @Override
    public CheckPoint getPoint(String unique) {
        return null;
    }

    @Override
    public List<CheckPoint> getPoints() {
        return null;
    }
}
