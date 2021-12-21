package org.apache.linkis.metadata.service;

import org.apache.linkis.metadata.util.DWSConfig;

import java.util.List;
import java.util.Map;

public interface HiveMetaWithPermissionService {

    List<String> getDbsOptionalUserName(String userName);

    List<Map<String, Object>> getTablesByDbNameAndOptionalUserName(Map<String, String> map);

}
