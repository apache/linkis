/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.message.publisher;

import com.webank.wedatasphere.linkis.common.utils.JavaLog;
import com.webank.wedatasphere.linkis.message.builder.DefaultServiceMethodContext;
import com.webank.wedatasphere.linkis.message.builder.MessageJob;
import com.webank.wedatasphere.linkis.message.builder.ServiceMethodContext;
import com.webank.wedatasphere.linkis.message.context.AbstractMessageSchedulerContext;
import com.webank.wedatasphere.linkis.message.exception.MessageWarnException;
import com.webank.wedatasphere.linkis.message.parser.ImplicitMethod;
import com.webank.wedatasphere.linkis.message.parser.ServiceMethod;
import com.webank.wedatasphere.linkis.message.scheduler.MethodExecuteWrapper;
import com.webank.wedatasphere.linkis.message.utils.MessageUtils;
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol;

import com.webank.wedatasphere.linkis.rpc.MessageErrorConstants;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.webank.wedatasphere.linkis.message.conf.MessageSchedulerConf.CONTEXT_KEY;


public abstract class AbstractMessagePublisher extends JavaLog implements MessagePublisher {

    private AbstractMessageSchedulerContext context;

    public AbstractMessagePublisher(AbstractMessageSchedulerContext context) {
        this.context = context;
    }

    public void setContext(AbstractMessageSchedulerContext context) {
        this.context = context;
    }

    /**
     * Key is the full class name of requestprotocol. In map, key is groupname
     * key是requestProtocol的全类名，Map中，key是groupName
     */
    private final Map<String, Map<String, List<ServiceMethod>>> protocolServiceMethodCache = new ConcurrentHashMap<>();


    @Override
    public MessageJob publish(RequestProtocol requestProtocol) {
        return publish(requestProtocol, new DefaultServiceMethodContext());
    }

    @Override
    public MessageJob publish(RequestProtocol requestProtocol, ServiceMethodContext serviceMethodContext) {
        logger().debug(String.format("receive request:%s", requestProtocol.getClass().getName()));
        serviceMethodContext.putIfAbsent(CONTEXT_KEY, this.context);
        Map<String, List<MethodExecuteWrapper>> methodExecuteWrappers = getMethodExecuteWrappers(requestProtocol);
        MessageJob messageJob = this.context.getJobBuilder().of()
                .with(serviceMethodContext).with(requestProtocol).with(this.context)
                .with(methodExecuteWrappers).build();
        this.context.getScheduler().submit(messageJob);
        return messageJob;
    }

    private Map<String, List<MethodExecuteWrapper>> getMethodExecuteWrappers(RequestProtocol requestProtocol) {
        String protocolName = requestProtocol.getClass().getName();
        Map<String, List<ServiceMethod>> protocolServiceMethods = this.protocolServiceMethodCache.get(protocolName);
        // Static information without locking （静态信息，无需加锁）
        if (protocolServiceMethods == null) {
            Map<String, List<ServiceMethod>> serviceMethodCache = this.context.getServiceRegistry().getServiceMethodCache();
            Map<String, List<ImplicitMethod>> implicitMethodCache = this.context.getImplicitRegistry().getImplicitMethodCache();
            // Find out that in the registration method, the parameter is the name of the parent class of the current request
            // 找出注册方法中，参数是当前请求的父类的
            Map<String, List<ServiceMethod>> serviceMatchs = serviceMethodCache.entrySet().stream()
                    .filter(e -> MessageUtils.isAssignableFrom(e.getKey(), protocolName))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            // Find out that in the implicit method, the parameter is the parent class of the current request.
            // According to the registration rules, the explicit parameter must not be repeated with the above servicematchkeys
            //找出implicit方法中，参数是当前请求父类的，根据注册规则，implicit的出参必然和上面的servicematchKeys 不会重复
            Map<String, List<ServiceMethod>> implicitMatchs = new HashMap<>();
            for (Map.Entry<String, List<ImplicitMethod>> implicitEntry : implicitMethodCache.entrySet()) {
                // In the current implicitmehtod, input needs to be the parent class or class of protocolname
                // 当前implicitMehtod中，input需要是protocolName 的父类or同类
                String implicitEntryKey = implicitEntry.getKey();
                List<ImplicitMethod> implicitEntryValue = implicitEntry.getValue();
                // Support the interface inheritance relationship between implicit return value and service
                // 支持隐式 返回值 和service之间的接口继承关系
                Map<String, List<ServiceMethod>> implicitServiceMethods = serviceMethodCache.entrySet().stream()
                        .filter(e -> MessageUtils.isAssignableFrom(e.getKey(), implicitEntryKey))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                // Exclude that the implicit return value is the parent of the protocolname, and there may be another irrelevant protocol.
                // The return value of the conversion method is the parent of the current protocol
                // 排除implicit返回值是protocolName的父类，可能存在另外一个不相干protocol的转换方法的返回值是当前protocol的父类
                if (!MessageUtils.isAssignableFrom(implicitEntryKey, protocolName) && !implicitServiceMethods.isEmpty()) {
                    for (Map.Entry<String, List<ServiceMethod>> implicitServiceMethodEntry : implicitServiceMethods.entrySet()) {
                        String implicitServiceMethodEntryKey = implicitServiceMethodEntry.getKey();
                        List<ServiceMethod> implicitServiceMethodEntryValue = implicitServiceMethodEntry.getValue();
                        // Parameter to support implicit
                        // 参数中要支持implicit的
                        List<ServiceMethod> filteredServiceMethods = implicitServiceMethodEntryValue.stream()
                                .filter(ServiceMethod::isAllowImplicit)
                                .collect(Collectors.toList());
                        // The parameter in the implicit method needs to be this class or subclass of the current request protocol
                        // 隐式方法中参数需要是当前请求protocol的本类或子类
                        List<ImplicitMethod> filteredImplicitMethods = implicitEntryValue.stream()
                                .filter(v -> MessageUtils.isAssignableFrom(v.getInput(), protocolName))
                                .collect(Collectors.toList());
                        if (!filteredServiceMethods.isEmpty() && !filteredImplicitMethods.isEmpty()) {
                            // Select for each servicemethod, because they may be between different services
                            // 针对每个ServiceMethod 选择，因为可能他们处于不同的service之间
                            for (ServiceMethod filteredServiceMethod : filteredServiceMethods) {
                                Object service = filteredServiceMethod.getService();
                                // Same as service priority
                                // 同service优先
                                Optional<ImplicitMethod> first = filteredImplicitMethods.stream()
                                        .filter(m -> m.getImplicitObject() == service).findFirst();
                                if (first.isPresent()) {
                                    filteredServiceMethod.setImplicitMethod(first.get());
                                } else {
                                    // TODO: 2020/7/30  The judgment priority of the parent-child class of
                                    //   the input parameter is consistent with that of scala
                                    // TODO: 入参父子类的判断优先级，和scala一致
                                    // Simply take the first one
                                    // 简单的只取第一个
                                    filteredServiceMethod.setImplicitMethod(filteredImplicitMethods.get(0));
                                }
                            }
                            // Add to cache
                            //添加到缓存中
                            implicitMatchs.put(implicitServiceMethodEntryKey, filteredServiceMethods);
                        }
                    }
                }
            }
            //merge
            serviceMatchs.putAll(implicitMatchs);
            // Group by chain name, flatten and then group. At this time, the parent class of protocol may be in the same chain as the transformed one
            // 按链名称分组， 扁平化后再group，这时protocol的父类可能和转换的处于同一个chain中
            serviceMatchs = serviceMatchs.values().stream().flatMap(Collection::stream).collect(Collectors.groupingBy(ServiceMethod::getChainName));
            //order判断
            for (List<ServiceMethod> value : serviceMatchs.values()) {
                Integer repeatOrder = MessageUtils.repeatOrder(value);
                if (repeatOrder != null && !MessageUtils.orderIsLast(repeatOrder, value)) {
                    throw new MessageWarnException(MessageErrorConstants.MESSAGE_ERROR(),
                            String.format("repeat "
                            + "order : %s for request %s", repeatOrder, protocolName));
                }
            }
            this.protocolServiceMethodCache.put(protocolName, serviceMatchs);
        }
        // Clone object and return
        // 克隆对象并返回
        return serviceMethod2Wrapper(this.protocolServiceMethodCache.get(protocolName));
    }

    private Map<String, List<MethodExecuteWrapper>> serviceMethod2Wrapper(Map<String, List<ServiceMethod>> source) {
        HashMap<String, List<MethodExecuteWrapper>> target = new HashMap<>();
        source.forEach((k, v) -> target.put(k, v.stream().map(MethodExecuteWrapper::new).collect(Collectors.toList())));
        return target;
    }


}
