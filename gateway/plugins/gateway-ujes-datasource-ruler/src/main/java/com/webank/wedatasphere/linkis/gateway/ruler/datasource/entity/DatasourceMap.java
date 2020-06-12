package com.webank.wedatasphere.linkis.gateway.ruler.datasource.entity;

import java.util.Objects;

/**
 * @author wang_zh
 * @date 2020/5/22
 */
public class DatasourceMap {

    public DatasourceMap() {}

    public DatasourceMap(String datasourceName, String instance, String serviceId) {
        this.datasourceName = datasourceName;
        this.instance = instance;
        this.serviceId = serviceId;
    }

    private String datasourceName;

    private String instance;

    private String serviceId;

    public String getDatasourceName() {
        return datasourceName;
    }

    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasourceMap that = (DatasourceMap) o;
        return Objects.equals(datasourceName, that.datasourceName) &&
                Objects.equals(instance, that.instance) &&
                Objects.equals(serviceId, that.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datasourceName, instance, serviceId);
    }
}
