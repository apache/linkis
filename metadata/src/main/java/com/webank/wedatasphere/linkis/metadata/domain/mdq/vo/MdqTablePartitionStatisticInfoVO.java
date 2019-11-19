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
