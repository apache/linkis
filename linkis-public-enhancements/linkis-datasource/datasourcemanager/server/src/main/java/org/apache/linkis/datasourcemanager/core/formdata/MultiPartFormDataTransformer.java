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
 
package org.apache.linkis.datasourcemanager.core.formdata;

import org.apache.linkis.common.exception.ErrorException;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import javax.validation.ValidationException;
import javax.validation.Validator;

/**
 * Transformer of multipart form
 */
public interface MultiPartFormDataTransformer {
    /**
     * Transform the form data to object and validate its fields
     * @param formData form data
     * @param clazz clazz
     * @param beanValidator validator
     * @param <T>
     * @return
     * @throws ValidationException
     * @throws ErrorException
     */
    <T>T transformToObject(FormDataMultiPart formData, Class<?> clazz, Validator beanValidator)
            throws ValidationException, ErrorException;

}
