/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.cs.common.entity.history.metadata;

import com.webank.wedatasphere.linkis.cs.common.entity.metadata.Table;

import java.util.List;


public class CSTableLineageHistory extends CSTableMetadataContextHistory implements TableLineageHistory {

    private List<Table> sourceTables;


    @Override
    public List<Table> getSourceTables() {
        return this.sourceTables;
    }


    @Override
    public void setSourceTables(List<Table> tables) {
        this.sourceTables = tables;
    }

    @Override
    public TableOperationType getOperationType() {
        return TableOperationType.CREATE;
    }
}
