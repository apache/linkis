package org.apache.linkis.manager.rm.external.kubernetes;

import org.apache.linkis.manager.common.entity.resource.ResourceType;
import org.apache.linkis.manager.rm.external.domain.ExternalResourceIdentifier;

public class KubernetesResourceIdentifier implements ExternalResourceIdentifier {
    @Override
    public ResourceType getResourceType() {
        return ResourceType.KubernetesResource;
    }
}
