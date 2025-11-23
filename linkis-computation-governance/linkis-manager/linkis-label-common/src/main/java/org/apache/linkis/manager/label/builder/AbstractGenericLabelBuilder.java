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

package org.apache.linkis.manager.label.builder;

import org.apache.linkis.manager.label.entity.CloneableLabel;
import org.apache.linkis.manager.label.entity.InheritableLabel;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.utils.LabelUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.*;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.manager.label.utils.LabelUtils.Jackson.*;

@SuppressWarnings("rawtypes")
public abstract class AbstractGenericLabelBuilder implements ExtensibleLabelBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractGenericLabelBuilder.class);

  /**
   * Transform value object
   *
   * @param rawValue value
   * @param parameterTypes parameters
   * @param <V> Any
   * @return result
   */
  @SuppressWarnings("unchecked")
  protected <V> V transformValueToConcreteType(
      Object rawValue, Type concreteType, Type[] parameterTypes, Function<String, V> deserializer) {
    if (null != rawValue) {
      try {
        Class<?> sourceClass = rawValue.getClass();
        Class<?> concreteClass = (Class<?>) concreteType;
        if (concreteClass.isAssignableFrom(sourceClass)) {
          if (CloneableLabel.class.isAssignableFrom(sourceClass)) {
            // Just clone label
            return (V) ((CloneableLabel) rawValue).clone();
          }
        } else if (LabelUtils.isBasicType(concreteClass)) {
          // Serialize the raw value first
          return (V) convert(toJson(rawValue, null), concreteClass);
        } else if (LabelUtils.isBasicType(rawValue.getClass())) {
          String rawString = String.valueOf(rawValue);
          if (null != deserializer) {
            return deserializer.apply(rawString);
          }
          JavaType javaType = getJavaType(concreteType, parameterTypes);
          if (concreteClass.equals(Map.class)) {
            try {
              return (V)
                  convert(fromJson(rawString, Map.class, Object.class, Object.class), javaType);
            } catch (Exception e) {
              LOG.trace("Transform raw value fail, return empty object", e);
              return fromJson("{}", javaType);
            }
          } else if (concreteClass.equals(List.class)) {
            try {
              return (V) convert(fromJson(rawString, List.class, Object.class), javaType);
            } catch (Exception e) {
              // Ignore
              LOG.trace("Transform raw value fail, return empty list", e);
              return fromJson("[]", javaType);
            }
          }
        }
        // Try to convert value directly
        return (V) LabelUtils.Jackson.convert(rawValue, getJavaType(concreteType, parameterTypes));
      } catch (Exception e) {
        // Ignore
        LOG.trace("Transform raw value fail, return null", e);
      }
    }
    return null;
  }

  /**
   * Find label's value class
   *
   * @param labelClass label class provided
   * @return class array
   */
  public Class<?>[] findActualLabelValueClass(Class<? extends Label> labelClass) {
    Map<Class<?>, Type[]> classTypeVariableMap = new HashMap<>();
    Type[] classTypes = recurseToFindActualLabelValueType(labelClass, classTypeVariableMap);
    if (null != classTypes) {
      // Transform to class object
      List<Class<?>> classesReturn = new ArrayList<>();
      // Expand combine types
      classTypes = expandParameterizedType(classTypes);
      for (Type classType : classTypes) {
        if (classType instanceof Class) {
          classesReturn.add((Class<?>) classType);
        } else if (classType instanceof WildcardType) {
          classesReturn.add(Object.class);
        }
      }
      return classesReturn.toArray(new Class<?>[0]);
    }
    return null;
  }

  /**
   * Find label's value type
   *
   * @param labelType label class provided
   * @return type array
   */
  public Type[] findActualLabelValueType(Class<? extends Label> labelType) {
    Map<Class<?>, Type[]> classTypeVariableMap = new HashMap<>();
    return recurseToFindActualLabelValueType(labelType, classTypeVariableMap);
  }
  /**
   * Recurse to find label's value type
   *
   * @param labelClass label class
   * @param classTypeVariableMap use to store the mapping relation between class and type
   * @return value type array
   */
  private Type[] recurseToFindActualLabelValueType(
      Class<? extends Label> labelClass, Map<Class<?>, Type[]> classTypeVariableMap) {
    if (null == labelClass) {
      return null;
    }
    // Store the reflect relation between parameter variable like 'T' and type like
    // 'java.lang.String'
    Map<String, Type> typeVariableReflect = new HashMap<>();
    Type[] typeParameters = labelClass.getTypeParameters();
    if (typeParameters.length > 0) {
      Type[] classTypes = classTypeVariableMap.get(labelClass);
      if (null == classTypes) {
        return null;
      }
      for (int i = 0; i < classTypes.length; i++) {
        typeVariableReflect.put(typeParameters[i].getTypeName(), classTypes[i]);
      }
    }
    if (labelClass.equals(Label.class) || labelClass.equals(InheritableLabel.class)) {
      return classTypeVariableMap.get(labelClass);
    }
    // Search from interfaces to super class
    Type[] valueTypes =
        findValueTypeFromInterface(labelClass, classTypeVariableMap, typeVariableReflect);
    if (null == valueTypes) {
      valueTypes =
          findValueTypeFromSuperclass(labelClass, classTypeVariableMap, typeVariableReflect);
    }
    return valueTypes;
  }

  /**
   * Find label's value type from interfaces
   *
   * @param labelClass label class
   * @param classTypeVariableMap stored the mapping relation between class and type
   * @param typeVariableReflect stored the mapping relation between type variables and actually
   *     types
   * @return value type array
   */
  @SuppressWarnings("unchecked")
  private Type[] findValueTypeFromInterface(
      Class<? extends Label> labelClass,
      Map<Class<?>, Type[]> classTypeVariableMap,
      Map<String, Type> typeVariableReflect) {
    Class<?>[] interfaces = labelClass.getInterfaces();
    if (interfaces.length > 0) {
      for (int i = 0; i < interfaces.length; i++) {
        Class<?> interfaceClass = interfaces[i];
        if (Label.class.isAssignableFrom(interfaceClass)) {
          // As the jdk doc, getInterfaces() and getGenericInterfaces() method has the
          // same order
          Type interfaceType = labelClass.getGenericInterfaces()[i];
          // If the interface type has type arguments, transform and store into
          // 'classTypeVariableMap'
          if (interfaceType instanceof ParameterizedType) {
            Type[] actualTypes = ((ParameterizedType) interfaceType).getActualTypeArguments();
            actualTypes = transformParameterizedType(actualTypes, typeVariableReflect);
            classTypeVariableMap.put(interfaceClass, actualTypes);
          }
          return recurseToFindActualLabelValueType(
              (Class<? extends Label>) interfaceClass, classTypeVariableMap);
        }
      }
    }
    return null;
  }

  /**
   * Find label's value type from super class
   *
   * @param label label class
   * @param classTypeVariableMap stored the mapping relation between class and type
   * @param typeVariableReflect stored the mapping relation between type variables and actually
   *     types
   * @return value type array
   */
  @SuppressWarnings("unchecked")
  private Type[] findValueTypeFromSuperclass(
      Class<? extends Label> label,
      Map<Class<?>, Type[]> classTypeVariableMap,
      Map<String, Type> typeVariableReflect) {
    Type superclassType = label.getGenericSuperclass();
    if (superclassType instanceof ParameterizedType) {
      Type[] actualTypes = ((ParameterizedType) superclassType).getActualTypeArguments();
      actualTypes = transformParameterizedType(actualTypes, typeVariableReflect);
      classTypeVariableMap.put(label.getSuperclass(), actualTypes);
    }
    return recurseToFindActualLabelValueType(
        (Class<? extends Label>) label.getSuperclass(), classTypeVariableMap);
  }

  /**
   * Do transform
   *
   * @param rawTypes raw types
   * @param typeVariableReflect stored the mapping relation between type variables and actually
   *     types
   * @return result
   */
  private Type[] transformParameterizedType(
      Type[] rawTypes, Map<String, Type> typeVariableReflect) {
    return resolveParameterizedType(
        rawTypes,
        (rawType) -> {
          if (rawType instanceof TypeVariable) {
            return typeVariableReflect.getOrDefault(rawType.getTypeName(), rawType);
          }
          return rawType;
        });
  }

  /**
   * Resolve parameterized type (parameterized type -> combined type)
   *
   * @param rawTypes unresolved types
   * @param resolveFunc resolve function
   * @return resolved types
   */
  private Type[] resolveParameterizedType(Type[] rawTypes, Function<Type, Type> resolveFunc) {
    Queue<Type> queue = new LinkedList<>();
    List<Type> resolvedTypes = new ArrayList<>();
    CombineType combineType = null;
    for (Type rawType : rawTypes) {
      queue.add(rawType);
      while (!queue.isEmpty()) {
        Type typeInQueue = queue.poll();
        // Change the combineType used currently
        if (typeInQueue instanceof CombineType) {
          combineType = (CombineType) typeInQueue;
          continue;
        }
        if (typeInQueue instanceof ParameterizedType) {
          // Construct a new combine type and add to queue
          CombineType newCombineType =
              new CombineType(((ParameterizedType) typeInQueue).getRawType());
          queue.add(newCombineType);
          Type[] actualTypes = ((ParameterizedType) typeInQueue).getActualTypeArguments();
          // Add the type arguments to queue
          queue.addAll(Arrays.asList(actualTypes));
          // Add the combineType exist to the queue
          if (null != combineType) {
            combineType.childTypes.add(newCombineType);
            queue.add(combineType);
          } else {
            resolvedTypes.add(newCombineType);
          }
        } else if (null != combineType) {
          combineType.childTypes.add(resolveFunc.apply(typeInQueue));
        } else {
          resolvedTypes.add(resolveFunc.apply(typeInQueue));
        }
      }
      combineType = null;
    }
    return resolvedTypes.toArray(new Type[0]);
  }

  /**
   * Expand combined types to array
   *
   * @param resolvedTypes combined types
   * @return types
   */
  private Type[] expandParameterizedType(Type[] resolvedTypes) {
    Queue<Type> queue = new LinkedList<>();
    List<Type> expandTypes = new ArrayList<>();
    for (Type expandType : resolvedTypes) {
      queue.add(expandType);
      while (!queue.isEmpty()) {
        Type typeInQueue = queue.poll();
        if (typeInQueue instanceof CombineType) {
          CombineType combineType = (CombineType) typeInQueue;
          expandTypes.add(combineType.actual);
          queue.addAll(combineType.childTypes);
        } else {
          expandTypes.add(typeInQueue);
        }
      }
    }
    return expandTypes.toArray(new Type[0]);
  }

  /**
   * Transform to Jackson's JavaType
   *
   * @param concreteType concrete type
   * @param parameterTypes parameter type
   * @return
   */
  private JavaType getJavaType(Type concreteType, Type... parameterTypes) {
    List<JavaType> javaTypes = new ArrayList<>();
    for (Type parameterType : parameterTypes) {
      if (parameterType instanceof CombineType) {
        CombineType combineType = (CombineType) parameterType;
        javaTypes.add(getJavaType(combineType.actual, combineType.childTypes.toArray(new Type[0])));
      } else {
        javaTypes.add(getJavaType(parameterType));
      }
    }
    if (javaTypes.size() > 0) {
      Class<?> rawClass = Object.class;
      if (concreteType instanceof Class) {
        rawClass = (Class<?>) concreteType;
      }
      return TypeFactory.defaultInstance()
          .constructParametricType(rawClass, javaTypes.toArray(new JavaType[0]));
    }
    return TypeFactory.defaultInstance().constructType(concreteType);
  }

  /** Combine different type object */
  protected static class CombineType implements Type {

    /** Actual type */
    protected Type actual;

    /** Children types */
    protected List<Type> childTypes = new ArrayList<>();

    CombineType(Type actual) {
      this.actual = actual;
    }
  }
}
