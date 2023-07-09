package org.apache.linkis.manager.rm.service.impl;

import org.apache.linkis.manager.common.entity.resource.ResourceType;
import org.apache.linkis.manager.rm.external.service.ExternalResourceService;
import org.apache.linkis.manager.rm.service.LabelResourceService;
import org.apache.linkis.manager.rm.service.RequestResourceService;

import static org.apache.linkis.manager.common.entity.resource.ResourceType.KubernetesResource;

public class KubernetesReqResourceService extends RequestResourceService {

    private LabelResourceService labelResourceService;
    private ExternalResourceService externalResourceService;
    private final ResourceType resourceType = KubernetesResource;

    public KubernetesReqResourceService(
            LabelResourceService labelResourceService, ExternalResourceService externalResourceService) {
        super(labelResourceService);
        this.labelResourceService = labelResourceService;
        this.externalResourceService = externalResourceService;
    }

    @Override
    public ResourceType resourceType() {
        return this.resourceType;
    }
}
