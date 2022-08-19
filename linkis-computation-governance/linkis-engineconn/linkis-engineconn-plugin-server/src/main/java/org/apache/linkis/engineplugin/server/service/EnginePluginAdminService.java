package org.apache.linkis.engineplugin.server.service;

import org.apache.linkis.bml.protocol.Version;
import org.apache.linkis.engineplugin.server.entity.EngineConnBmlResource;
import org.apache.linkis.engineplugin.vo.EnginePluginBMLVo;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import com.github.pagehelper.PageInfo;

public interface EnginePluginAdminService {

  void rollBackEnginePlugin(EngineConnBmlResource engineConnBmlResource);

  void uploadToECHome(MultipartFile file);

  void deleteEnginePluginBML(String ecType, String version, String username);

  PageInfo<EngineConnBmlResource> queryDataSourceInfoPage(EnginePluginBMLVo enginePluginBMLVo);

  List<String> getTypeList();

  List<String> getTypeVersionList(String type);

  List<Version> getVersionList(String userName, String bmlResourceId);
}
