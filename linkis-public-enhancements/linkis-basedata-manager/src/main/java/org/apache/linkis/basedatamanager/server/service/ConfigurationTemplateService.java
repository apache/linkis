package org.apache.linkis.basedatamanager.server.service;

import org.apache.linkis.basedatamanager.server.request.ConfigurationTemplateSaveRequest;

/**
 * This module is designed to manage configuration parameter templates
 */
public interface ConfigurationTemplateService {

    /**
     * save a configuration template
     *
     * @param request ConfigurationTemplateSaveRequest
     * @return Boolean
     */
    Boolean saveConfigurationTemplate(ConfigurationTemplateSaveRequest request);

    /**
     * delete a configuration template
     *
     * @param keyId Long
     * @return Boolean
     */
    Boolean deleteConfigurationTemplate(Long keyId);
}
