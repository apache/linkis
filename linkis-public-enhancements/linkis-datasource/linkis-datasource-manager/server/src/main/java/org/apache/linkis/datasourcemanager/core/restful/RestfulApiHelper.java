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

package org.apache.linkis.datasourcemanager.core.restful;

import org.apache.linkis.common.exception.WarnException;
import org.apache.linkis.datasourcemanager.common.auth.AuthContext;
import org.apache.linkis.datasourcemanager.common.domain.DataSourceParamKeyDefinition;
import org.apache.linkis.datasourcemanager.core.restful.exception.BeanValidationExceptionMapper;
import org.apache.linkis.datasourcemanager.core.validate.ParameterValidateException;
import org.apache.linkis.server.Message;

import org.apache.commons.codec.binary.Base64;

import javax.validation.ConstraintViolationException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Helper of restful api entrance */
public class RestfulApiHelper {
  /**
   * If is administrator
   *
   * @param userName user name
   * @return whether userName is an admin user
   */
  public static boolean isAdminUser(String userName) {
    List<String> userList = Arrays.asList(AuthContext.AUTH_ADMINISTRATOR.getValue().split(","));
    return userList.contains(userName);
  }

  /**
   * If is not administrator
   *
   * @param userName user name
   * @return whether userName is not an admin user
   */
  public static boolean isNotAdminUser(String userName) {
    return !isAdminUser(userName);
  }

  /**
   * Encrypt key of password type
   *
   * @param keyDefinitionList definition list
   * @param connectParams connection parameters
   */
  public static void encryptPasswordKey(
      List<DataSourceParamKeyDefinition> keyDefinitionList, Map<String, Object> connectParams) {
    keyDefinitionList.forEach(
        keyDefinition -> {
          if (keyDefinition.getValueType() == DataSourceParamKeyDefinition.ValueType.PASSWORD) {
            String password = String.valueOf(connectParams.get(keyDefinition.getKey()));
            if (null != password) {
              connectParams.put(
                  keyDefinition.getKey(), new String(new Base64().encode(password.getBytes())));
            }
          }
        });
  }

  /**
   * Encrypt key of password type
   *
   * @param keyDefinitionList definition list
   * @param connectParams connection parameters
   */
  public static void decryptPasswordKey(
      List<DataSourceParamKeyDefinition> keyDefinitionList, Map<String, Object> connectParams) {
    keyDefinitionList.forEach(
        keyDefinition -> {
          if (keyDefinition.getValueType() == DataSourceParamKeyDefinition.ValueType.PASSWORD) {
            String password = String.valueOf(connectParams.get(keyDefinition.getKey()));
            if (null != password) {
              connectParams.put(
                  keyDefinition.getKey(), new String(new Base64().decode(password.getBytes())));
            }
          }
        });
  }

  /**
   * @param tryOperation operate function
   * @param failMessage message
   */
  public static Message doAndResponse(TryOperation tryOperation, String failMessage) {
    try {
      Message message = tryOperation.operateAndGetMessage();
      return message;
    } catch (ParameterValidateException e) {
      return Message.error(e.getMessage());
    } catch (ConstraintViolationException e) {
      return new BeanValidationExceptionMapper().toResponse(e);
    } catch (WarnException e) {
      return Message.warn(e.getMessage());
    } catch (Exception e) {
      return Message.error(failMessage, e);
    }
  }

  //    /**
  //     * @param tryOperation operate function
  //     * @param failMessage message
  //     */
  //    public static Message doAndResponse(
  //            TryOperation tryOperation, String method, String failMessage) {
  //        try {
  //            Message message = tryOperation.operateAndGetMessage();
  //            return setMethod(message, method);
  //        } catch (ParameterValidateException e) {
  //            return setMethod(Message.error(e.getMessage()), method);
  //        } catch (ConstraintViolationException e) {
  //            return new BeanValidationExceptionMapper().toResponse(e);
  //        } catch (WarnException e) {
  //            return setMethod(Message.warn(e.getMessage()), method);
  //        } catch (Exception e) {
  //            return setMethod(Message.error(failMessage, e), method);
  //        }
  //    }

  private static Message setMethod(Message message, String method) {
    message.setMethod(method);
    return message;
  }

  @FunctionalInterface
  public interface TryOperation {

    /** Operate method */
    Message operateAndGetMessage() throws Exception;
  }
}
