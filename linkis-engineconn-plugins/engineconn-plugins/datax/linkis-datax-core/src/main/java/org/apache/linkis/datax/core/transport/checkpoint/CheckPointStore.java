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

package org.apache.linkis.datax.core.transport.checkpoint;


import java.util.List;

/**
 * @author davidhua
 * 2020/3/18
 */
public interface CheckPointStore {

    /**
     * Save checkpoint
     * @param checkPoint checkpoint entity
     */
    void savePoint(CheckPoint checkPoint);

    /**
     * Get checkpoint
     * @param unique unique identify
     * @return
     */
    CheckPoint getPoint(String unique);

    /**
     * Get all checkpoints
     * @return
     */
    List<CheckPoint> getPoints();
}
