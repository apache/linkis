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
 
package org.apache.linkis.message;

import org.apache.linkis.message.annotation.*;
import org.apache.linkis.message.builder.ServiceMethodContext;
import org.apache.linkis.protocol.message.RequestProtocol;

import java.util.ArrayList;
import java.util.List;

/**
 * @date 2020/7/14
 */
public class TestService {
    @Receiver
    @Order(1)
    public void method01(ImplicitInterface protocol) throws InterruptedException {
        Thread.sleep(5000);
        System.out.println("TestService1.method01");
    }

    @Receiver
    public void method02(ServiceMethodContext smc, ImplicitInterface protocol) throws InterruptedException {
        Thread.sleep(2000);
        System.out.println("TestService1.method02");
    }

    @Receiver
    @Chain("fgf")
    public void method03(ServiceMethodContext smc, ImplicitInterface protocol) throws InterruptedException {
        Thread.sleep(3000);
       System.out.println("TestService1.method03");
    }

    @Receiver
    public List<Object> method04( ServiceMethodContext smc, @NotImplicit DefaultRequestProtocol protocol) throws InterruptedException {
        Thread.sleep(2000);
        System.out.println("TestService1.method04");
        return new ArrayList<>();
    }

    @Implicit
    public ImplicitInterfaceImpl implicitMethod02(DefaultRequestProtocol requestProtocol) {
        return null;
    }

    /**
     * 测试 转换方法的优先级
     *
     * @param protocol
     * @return
     */
    @Implicit
    public ImplicitInterfaceImpl implicitMetho01(RequestProtocol protocol) {
        return null;
    }


}
