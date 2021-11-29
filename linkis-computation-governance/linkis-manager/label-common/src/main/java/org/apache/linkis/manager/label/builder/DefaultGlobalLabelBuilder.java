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
 
package org.apache.linkis.manager.label.builder;

import org.apache.linkis.common.utils.ClassUtils;
import org.apache.linkis.manager.label.constant.LabelConstant;
import org.apache.linkis.manager.label.entity.InheritableLabel;
import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.SerializableLabel;
import org.apache.linkis.manager.label.entity.annon.KeyMethod;
import org.apache.linkis.manager.label.exception.LabelErrorException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * To build different types of label
 */
@SuppressWarnings(value= {"unchecked", "rawtypes"})
public class DefaultGlobalLabelBuilder extends AbstractGenericLabelBuilder{

    private static final Logger LOG = LoggerFactory.getLogger(DefaultGlobalLabelBuilder.class);


    private static final Function<String, Object> DEFAULT_STRING_DESERIALIZER = stringValue -> stringValue;

    private static final Map<String, Class<? extends Label<?>>> LABEL_KEY_TYPE_MAP = new HashMap<>();

    static{

        //Lazy load all label keys of label entities
        //Scan methods in Label to find annotation @LabelKey
        Method[] methods = Label.class.getMethods();
        Method getKeyMethod = null;
        for(Method method : methods){
            if(method.isAnnotationPresent(KeyMethod.class)){
                getKeyMethod = method;
            }
        }
        if(null != getKeyMethod) {
            @SuppressWarnings("rawtypes")
            Set<Class<? extends Label>> labelEntities = ClassUtils.reflections().getSubTypesOf(Label.class);
            Method finalGetKeyMethod = getKeyMethod;
            labelEntities.forEach((labelEntity) -> {
                //New instance and then invoke get_key method
                if(!Modifier.isInterface(labelEntity.getModifiers()) &&
                        !Modifier.isAbstract(labelEntity.getModifiers())) {
                    try {
                        Label<?> label = labelEntity.newInstance();
                        String labelKey = (String) finalGetKeyMethod.invoke(label);
                        if (StringUtils.isNotBlank(labelKey)) {
                            LABEL_KEY_TYPE_MAP.put(labelKey, (Class<? extends Label<?>>) labelEntity);
                        }
                    } catch (InstantiationException | IllegalAccessException e) {
                        LOG.info("Fail to reflect to new a label instance: [" + labelEntity.getSimpleName() + "]", e);
                    } catch (InvocationTargetException e) {
                        LOG.info("Fail to invoke method: [" + finalGetKeyMethod.getName()
                                + "] of label instance: [" + labelEntity.getSimpleName() + "]", e);
                    }
                }
            });
        }
    }

    @Override
    public boolean canBuild(String labelKey, Class<?> labelClass) {
        //Support all sub classes/interfaces of Label
        return null == labelClass || Label.class.isAssignableFrom(labelClass);
    }

    @Override
    public <T extends Label<?>> T build(String labelKey, Object valueObj, Class<?> labelClass, Type... valueTypes) throws LabelErrorException {
        Class<? extends Label> suitableLabelClass = getSuitableLabelClass(labelKey, labelClass);
        if(null != suitableLabelClass){
            Type suitableValueType = getSuitableValueType(suitableLabelClass, valueTypes);
            if(null != suitableValueType){
                return buildInner(labelKey, valueObj, suitableLabelClass, suitableValueType);
            }
        }
        return null;
    }


    @Override
    public <T extends Label<?>> T build(String labelKey, Object valueObj, Class<T> labelType) throws LabelErrorException {
        return build(labelKey, valueObj, (Class<?>)labelType);
    }

    @Override
    public <T extends Label<?>> T build(String labelKey, InputStream valueInput, Class<?> labelClass, Type... valueTypes) throws LabelErrorException {
        try {
            return build(labelKey, null == valueInput ? "" : IOUtils.toString(valueInput), labelClass, valueTypes);
        }catch(IOException e){
            throw new LabelErrorException(LabelConstant.LABEL_BUILDER_ERROR_CODE, "Fail to read value input stream", e);
        }
    }


    @Override
    public <T extends Label<?>> T build(String labelKey, InputStream valueInput, Class<T> labelClass) throws LabelErrorException {
        try{
            return build(labelKey, null == valueInput ? "" : IOUtils.toString(valueInput), (Class<?>) labelClass);
        }catch(IOException e){
            throw new LabelErrorException(LabelConstant.LABEL_BUILDER_ERROR_CODE, "Fail to read value input stream", e);
        }
    }

    @Override
    public int getOrder() {
        //At the end of process chain
        return Integer.MAX_VALUE;
    }

    /**
     * Internal method
     * @param labelKey label key
     * @param valueObj value object
     * @param suitableLabelClass suitable label class
     * @param suitableValueType suitable value type
     * @param <T>
     * @return
     * @throws LabelErrorException
     */
    protected <T extends Label<?>> T buildInner(String labelKey, Object valueObj,Class<? extends Label> suitableLabelClass,
                                                Type suitableValueType) throws LabelErrorException {
        //Transform valueObj and new instance
        if(suitableValueType instanceof CombineType){
            CombineType suitableCombineValueType = (CombineType)suitableValueType;
            return (T) newInstance(suitableLabelClass, labelKey, suitableCombineValueType.actual, transformValueToConcreteType(valueObj,
                    suitableCombineValueType.actual, suitableCombineValueType.childTypes.toArray(new Type[0]), DEFAULT_STRING_DESERIALIZER));
        }else{
            return (T)newInstance(suitableLabelClass, labelKey, suitableValueType, transformValueToConcreteType(valueObj, suitableValueType, new Type[]{}, null));
        }
    }

    protected Class<? extends Label> getSuitableLabelClass(String labelKey, Class<?> labelClass){
        if(null != labelClass) {
            if (Modifier.isInterface(labelClass.getModifiers()) || Modifier.isAbstract(labelClass.getModifiers())) {
                if (labelClass.equals(Label.class)) {
                    //Use default label type
                    return InheritableLabel.class;
                }
                //Random to choose a subclass for other sub interfaces of label
                Set<Class<?>> setLabel = ClassUtils.reflections().getSubTypesOf((Class<Object>) labelClass);
                for (Class<?> suitableFound : setLabel) {
                    if (!Modifier.isInterface(suitableFound.getModifiers())) {
                        return (Class<? extends Label>) suitableFound;
                    }
                }
                return null;
            }else{
                return (Class<? extends Label>)labelClass;
            }
        }
        //At last, try to get the suitable label type from LABEL_KEY_TYPE_MAP
        return LABEL_KEY_TYPE_MAP.getOrDefault(labelKey, null);
    }

    protected Type getSuitableValueType(Class<? extends Label> labelClass, Type... valueTypes){
        //Find the actual value types from class's metadata
        Type[] actualValueType = findActualLabelValueType(labelClass);
        if(null == actualValueType){
            //If not find, use the types defined by user
            actualValueType = valueTypes;
        }
        if(null == actualValueType){
            CombineType defaultType = new CombineType(Map.class);
            defaultType.childTypes.add(String.class);
            defaultType.childTypes.add(Object.class);
            actualValueType = new Type[]{defaultType};
        }
        return actualValueType[0];
    }

    private Label<?> newInstance(Class<? extends Label> labelType,String labelKey, Type labelValueType, Object labelValue) throws LabelErrorException {
        try {
            Label newLabel = labelType.newInstance();
            if(newLabel instanceof InheritableLabel){
                InheritableLabel inheritableNewLabel = (InheritableLabel)newLabel;
                //Hold back the label key existed
                if(null != labelKey && null == newLabel.getLabelKey()) {
                    inheritableNewLabel.setLabelKey(labelKey);
                }
                //For string value, invoke setStringValue() method
                boolean setString = false;
                if(null != labelValue && labelValue.getClass().equals(String.class)){
                    String SET_STRING_VALUE_METHOD = "setStringValue";
                    try {
                        Method method;
                        try {
                            method = SerializableLabel.class.getDeclaredMethod(SET_STRING_VALUE_METHOD, String.class);
                        }catch(NoSuchMethodException e){
                            method = labelType.getDeclaredMethod(SET_STRING_VALUE_METHOD, String.class);
                        }
                        method.setAccessible(true);
                        method.invoke(newLabel, String.valueOf(labelValue));
                        setString = true;
                    } catch (NoSuchMethodException noe) {
                        //Ignore
                    }
                }
                Class<?> labelValueClass = (Class<?>)labelValueType;
                if(null != labelValue && labelValueClass.isAssignableFrom(labelValue.getClass())
                    && (!setString || null == newLabel.getValue())) {
                    String SET_VALUE_METHOD = "setValue";
                    try {
                        Method method = InheritableLabel.class.getDeclaredMethod(SET_VALUE_METHOD, Object.class);
                        method.setAccessible(true);
                        method.invoke(newLabel, labelValue);
                    } catch (NoSuchMethodException noe) {
                        //Ignore
                    }
                }
            }
            return newLabel;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
           throw new LabelErrorException(LabelConstant.LABEL_BUILDER_ERROR_CODE, "Fail to construct a label instance of ["
                   + labelType.getSimpleName() + "]", e);
        }
    }
}
