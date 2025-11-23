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

package org.apache.linkis.cs.persistence.aop;

import org.apache.linkis.DataWorkCloudApplication;
import org.apache.linkis.cs.persistence.conf.PersistenceConf;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
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
public class PersistenceTuningAspect {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  private boolean tuningIsOpen = false;

  private Object tuningObject = null;

  private Method tuningMethod = null;

  private boolean isInited = false;

  public void init() {
    synchronized (this) {
      if (!isInited) {
        try {
          Class<?> tuningClass =
              this.getClass().getClassLoader().loadClass(PersistenceConf.TUNING_CLASS.getValue());
          ApplicationContext context = DataWorkCloudApplication.getApplicationContext();
          if (context != null) {
            try {
              tuningObject = context.getBean(tuningClass);
              logger.info("find singleton tuning Object from IOC");
            } catch (NoSuchBeanDefinitionException e) {
              logger.info("can not find singleton  tuning Object from IOC");
            }
          }
          if (tuningObject == null) {
            tuningObject = tuningClass.newInstance();
          }
          tuningMethod =
              tuningClass.getMethod(PersistenceConf.TUNING_METHOD.getValue(), Object.class);
          tuningIsOpen = true;
        } catch (ClassNotFoundException
            | InstantiationException
            | IllegalAccessException
            | NoSuchMethodException e) {
          logger.warn("can not load tuning class,tuning is close", e);
        } finally {
          isInited = true;
        }
      }
    }
  }

  @Pointcut(value = "@annotation(org.apache.linkis.cs.persistence.annotation.Tuning)")
  private void cut() {}

  @Around("cut()")
  public Object around(ProceedingJoinPoint point) throws Throwable {
    if (!isInited) {
      init();
    }
    if (!tuningIsOpen) {
      logger.info("tuning is close..return the real");
      return point.proceed();
    }
    Signature signature = point.getSignature();
    if (!(signature instanceof MethodSignature)) {
      throw new IllegalArgumentException("This annotation can only be used for methods(该注解只能用于方法)");
    }
    MethodSignature methodSignature = (MethodSignature) signature;
    Object target = point.getTarget();
    Method currentMethod =
        target.getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
    logger.info("call method (调用方法)：" + currentMethod.getName());
    return tuningMethod.invoke(tuningObject, point.proceed());
  }
}
