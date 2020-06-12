package com.webank.wedatasphere.linkis.gateway.ruler.datasource.dao;

import com.webank.wedatasphere.linkis.gateway.ruler.datasource.entity.DatasourceMap;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author wang_zh
 * @date 2020/5/22
 */
@Repository
public interface DatasourceMapMapper {

    void createTableIfNotExists();

    List<DatasourceMap> listAll();

    void insert(DatasourceMap datasourceMap);

    long countByInstance(@Param("instance") String instance);

    DatasourceMap getByDatasource(@Param("datasourceName") String datasourceName);

    void cleanBadInstances(@Param("badInstances") Set<String> badInstances);

}
