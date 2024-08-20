/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.instance.label.service.impl;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.instance.label.exception.InstanceErrorException;
import org.apache.linkis.instance.label.service.InsLabelAccessService;
import org.apache.linkis.instance.label.service.InsLabelServiceAdapter;
import org.apache.linkis.manager.label.entity.Label;

import javax.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultInsLabelServiceAdapter implements InsLabelServiceAdapter {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultInsLabelServiceAdapter.class);

  private List<AccessServiceContext> serviceContexts = new ArrayList<>();

  @PostConstruct
  public void init() {
    this.serviceContexts.sort(
        (left, right) -> {
          int compare = left.order - right.order;
          if (compare == 0) {
            if (DefaultInsLabelService.class.isAssignableFrom(left.accessService.getClass())) {
              return -1;
            } else if (DefaultInsLabelService.class.isAssignableFrom(
                right.accessService.getClass())) {
              return 1;
            }
          }
          return compare;
        });
  }

  @Override
  public void registerServices(InsLabelAccessService instanceService) {
    registerServices(instanceService, -1);
  }

  @Override
  public void registerServices(InsLabelAccessService insLabelAccessService, int order) {
    serviceContexts.add(new AccessServiceContext(insLabelAccessService, order));
  }

  @Override
  public void attachLabelToInstance(Label<?> label, ServiceInstance serviceInstance) {
    execOnServiceChain(
        "attachLabelToInstance",
        insLabelAccessService -> {
          try {
            insLabelAccessService.attachLabelToInstance(label, serviceInstance);
          } catch (InstanceErrorException e) {
            LOG.error("Failed to attachLabelToInstance", e);
          }
          return true;
        },
        true,
        true);
  }

  @Override
  public void attachLabelsToInstance(
      List<? extends Label<?>> labels, ServiceInstance serviceInstance) {
    execOnServiceChain(
        "attachLabelsToInstance",
        insLabelAccessService -> {
          try {
            insLabelAccessService.attachLabelsToInstance(labels, serviceInstance);
          } catch (InstanceErrorException e) {
            LOG.error("Failed to attachLabelToInstance", e);
          }
          return true;
        },
        true,
        true);
  }

  @Override
  public void refreshLabelsToInstance(
      List<? extends Label<?>> labels, ServiceInstance serviceInstance) {
    execOnServiceChain(
        "refreshLabelsToInstance",
        insLabelAccessService -> {
          try {
            insLabelAccessService.refreshLabelsToInstance(labels, serviceInstance);
          } catch (InstanceErrorException e) {
            LOG.error("Failed to attachLabelToInstance", e);
          }
          return true;
        },
        true,
        true);
  }

  @Override
  public void removeLabelsFromInstance(ServiceInstance serviceInstance) {
    execOnServiceChain(
        "removeLabelsFromInstance",
        insLabelAccessService -> {
          insLabelAccessService.removeLabelsFromInstance(serviceInstance);
          return true;
        },
        true,
        true);
  }

  @Override
  public List<ServiceInstance> searchInstancesByLabels(List<? extends Label<?>> labels) {
    return execOnServiceChain(
        "searchInstancesByLabels",
        insLabelAccessService ->
            insLabelAccessService.searchInstancesByLabels(labels, Label.ValueRelation.ALL),
        false,
        false);
  }

  @Override
  public List<ServiceInstance> searchUnRelateInstances(ServiceInstance serviceInstance) {
    return execOnServiceChain(
        "searchUnRelateInstances",
        insLabelAccessService -> insLabelAccessService.searchUnRelateInstances(serviceInstance),
        false,
        false);
  }

  @Override
  public List<ServiceInstance> searchLabelRelatedInstances(ServiceInstance serviceInstance) {
    return execOnServiceChain(
        "searchLabelRelatedInstances",
        insLabelAccessService -> insLabelAccessService.searchLabelRelatedInstances(serviceInstance),
        false,
        false);
  }

  @Override
  public void removeInstance(ServiceInstance serviceInstance) {
    execOnServiceChain(
        "removeInstance",
        insLabelAccessService -> {
          insLabelAccessService.removeInstance(serviceInstance);
          return true;
        },
        true,
        true);
  }

  @Override
  public List<ServiceInstance> getInstancesByNames(String appName) {
    return execOnServiceChain(
        "getInstancesByNames",
        insLabelAccessService -> insLabelAccessService.getInstancesByNames(appName),
        false,
        false);
  }

  @Override
  public void evictCache() {
    // Empty
  }

  /**
   * Execute the function on services' chain
   *
   * @param execFunc function
   * @param isFilter if is true, will iterator all the services on the chain
   * @param allSuccess if is true, will require all the callings is successful
   * @param <R> return type
   * @return result
   */
  private <R> R execOnServiceChain(
      String funcName,
      Function<InsLabelAccessService, R> execFunc,
      boolean isFilter,
      boolean allSuccess) {
    R returnStored = null;
    List<Throwable> errors = new ArrayList<>();
    for (int i = 0; i < serviceContexts.size(); i++) {
      AccessServiceContext context = serviceContexts.get(i);
      try {
        R callResult = context.exec(execFunc);
        if (null == returnStored) {
          // Only store the result of service which is in the head of chain
          returnStored = callResult;
        }
        if (!isFilter) {
          break;
        }
      } catch (Throwable e) {
        LOG.error(
            "Execute ["
                + funcName
                + "] on service: [ "
                + context.accessService.getClass().getSimpleName()
                + "] failed, "
                + "message: ["
                + e.getMessage()
                + "]",
            e);
        errors.add(e);
        if (allSuccess
            || (i <= serviceContexts.size() - 1 && errors.size() == serviceContexts.size())) {
          // TODO throw exception
        }
      }
    }
    return returnStored;
  }

  private static class AccessServiceContext {
    private InsLabelAccessService accessService;

    private int order;

    AccessServiceContext(InsLabelAccessService accessService, int order) {
      this.accessService = accessService;
      this.order = order;
    }

    public <R> R exec(Function<InsLabelAccessService, R> execFunc) {
      return execFunc.apply(this.accessService);
    }
  }
}
