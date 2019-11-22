/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.metadata.domain.mdq.vo;

import java.util.ArrayList;
import java.util.List;


public class MdqTablePartitionStatisticInfoVO {
    private Integer fileNum;
    private String partitionSize;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private List<MdqTablePartitionStatisticInfoVO> childrens = new ArrayList<>();


    public List<MdqTablePartitionStatisticInfoVO> getChildrens() {
        return childrens;
    }

    public void setChildrens(List<MdqTablePartitionStatisticInfoVO> childrens) {
        this.childrens = childrens;
    }

    public Integer getFileNum() {
        return fileNum;
    }

    public void setFileNum(Integer fileNum) {
        this.fileNum = fileNum;
    }

    public String getPartitionSize() {
        return partitionSize;
    }

    public void setPartitionSize(String partitionSize) {
        this.partitionSize = partitionSize;
    }
}
