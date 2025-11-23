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

package org.apache.linkis.metadata.hive.config;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
@Component
public class MultiSourceExAop implements Ordered {

  private Logger log = LoggerFactory.getLogger(this.getClass());

  @Pointcut(value = "@annotation(org.apache.linkis.metadata.hive.config.DataSource)")
  private void cut() {}

  @Around("cut()")
  public Object around(ProceedingJoinPoint point) throws Throwable {

    Signature signature = point.getSignature();
    MethodSignature methodSignature = null;
    if (!(signature instanceof MethodSignature)) {
      throw new IllegalArgumentException("该注解只能用于方法");
    }
    methodSignature = (MethodSignature) signature;

    Object target = point.getTarget();
    Method currentMethod =
        target.getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());

    DataSource datasource = currentMethod.getAnnotation(DataSource.class);
    if (datasource != null) {
      DataSourceContextHolder.setDataSourceType(datasource.name());
      log.debug("设置数据源为：" + datasource.name());
    } else {
      DataSourceContextHolder.setDataSourceType(DSEnum.SECONDE_DATA_SOURCE);
      log.debug("设置数据源为：hiveDataSource");
    }
    try {
      return point.proceed();
    } finally {
      log.debug("清空数据源信息！");
      DataSourceContextHolder.clearDataSourceType();
    }
  }

  @Override
  public int getOrder() {
    return 1;
  }
}
