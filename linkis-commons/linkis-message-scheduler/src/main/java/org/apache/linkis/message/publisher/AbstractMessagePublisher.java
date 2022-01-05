/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.message.publisher;

import org.apache.linkis.common.utils.JavaLog;
import org.apache.linkis.message.builder.DefaultServiceMethodContext;
import org.apache.linkis.message.builder.MessageJob;
import org.apache.linkis.message.builder.ServiceMethodContext;
import org.apache.linkis.message.context.AbstractMessageSchedulerContext;
import org.apache.linkis.message.exception.MessageWarnException;
import org.apache.linkis.message.parser.ImplicitMethod;
import org.apache.linkis.message.parser.ServiceMethod;
import org.apache.linkis.message.scheduler.MethodExecuteWrapper;
import org.apache.linkis.message.utils.MessageUtils;
import org.apache.linkis.protocol.message.RequestProtocol;

import org.apache.linkis.rpc.MessageErrorConstants;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.apache.linkis.message.conf.MessageSchedulerConf.CONTEXT_KEY;


public abstract class AbstractMessagePublisher extends JavaLog implements MessagePublisher {

    private AbstractMessageSchedulerContext context;

    public AbstractMessagePublisher(AbstractMessageSchedulerContext context) {
        this.context = context;
    }

    public void setContext(AbstractMessageSchedulerContext context) {
        this.context = context;
    }

    /**
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
        //静态信息，无需加锁
        if (protocolServiceMethods == null) {
            Map<String, List<ServiceMethod>> serviceMethodCache = this.context.getServiceRegistry().getServiceMethodCache();
            Map<String, List<ImplicitMethod>> implicitMethodCache = this.context.getImplicitRegistry().getImplicitMethodCache();
            //找出注册方法中，参数是当前请求的父类的
            Map<String, List<ServiceMethod>> serviceMatchs = serviceMethodCache.entrySet().stream()
                    .filter(e -> MessageUtils.isAssignableFrom(e.getKey(), protocolName))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            //找出implicit方法中，参数是当前请求父类的，根据注册规则，implicit的出参必然和上面的servicematchKeys 不会重复
            Map<String, List<ServiceMethod>> implicitMatchs = new HashMap<>();
            for (Map.Entry<String, List<ImplicitMethod>> implicitEntry : implicitMethodCache.entrySet()) {
                //当前implicitMehtod中，input需要是protocolName 的父类or同类
                String implicitEntryKey = implicitEntry.getKey();
                List<ImplicitMethod> implicitEntryValue = implicitEntry.getValue();
                // 支持隐式 返回值 和service之间的接口继承关系
                Map<String, List<ServiceMethod>> implicitServiceMethods = serviceMethodCache.entrySet().stream()
                        .filter(e -> MessageUtils.isAssignableFrom(e.getKey(), implicitEntryKey))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                //排除implicit返回值是protocolName的父类，可能存在另外一个不相干protocol的转换方法的返回值是当前protocol的父类
                if (!MessageUtils.isAssignableFrom(implicitEntryKey, protocolName) && !implicitServiceMethods.isEmpty()) {
                    for (Map.Entry<String, List<ServiceMethod>> implicitServiceMethodEntry : implicitServiceMethods.entrySet()) {
                        String implicitServiceMethodEntryKey = implicitServiceMethodEntry.getKey();
                        List<ServiceMethod> implicitServiceMethodEntryValue = implicitServiceMethodEntry.getValue();
                        //参数中要支持implicit的
                        List<ServiceMethod> filteredServiceMethods = implicitServiceMethodEntryValue.stream()
                                .filter(ServiceMethod::isAllowImplicit)
                                .collect(Collectors.toList());
                        //隐式方法中参数需要是当前请求protocol的本类或子类
                        List<ImplicitMethod> filteredImplicitMethods = implicitEntryValue.stream()
                                .filter(v -> MessageUtils.isAssignableFrom(v.getInput(), protocolName))
                                .collect(Collectors.toList());
                        if (!filteredServiceMethods.isEmpty() && !filteredImplicitMethods.isEmpty()) {
                            //针对每个ServiceMethod 选择，因为可能他们处于不同的service之间
                            for (ServiceMethod filteredServiceMethod : filteredServiceMethods) {
                                Object service = filteredServiceMethod.getService();
                                //同service优先
                                Optional<ImplicitMethod> first = filteredImplicitMethods.stream()
                                        .filter(m -> m.getImplicitObject() == service).findFirst();
                                if (first.isPresent()) {
                                    filteredServiceMethod.setImplicitMethod(first.get());
                                } else {
                                    // TODO: 2020/7/30  入参父子类的判断优先级，和scala一致
                                    //简单的只取第一个
                                    filteredServiceMethod.setImplicitMethod(filteredImplicitMethods.get(0));
                                }
                            }
                            //添加到缓存中
                            implicitMatchs.put(implicitServiceMethodEntryKey, filteredServiceMethods);
                        }
                    }
                }
            }
            //merge
            serviceMatchs.putAll(implicitMatchs);
            //group by chain name 扁平化后再group，这时protocol的父类可能和转换的处于同一个chain中
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
        //clone 对象并返回
        return serviceMethod2Wrapper(this.protocolServiceMethodCache.get(protocolName));
    }

    private Map<String, List<MethodExecuteWrapper>> serviceMethod2Wrapper(Map<String, List<ServiceMethod>> source) {
        HashMap<String, List<MethodExecuteWrapper>> target = new HashMap<>();
        source.forEach((k, v) -> target.put(k, v.stream().map(MethodExecuteWrapper::new).collect(Collectors.toList())));
        return target;
    }


}
