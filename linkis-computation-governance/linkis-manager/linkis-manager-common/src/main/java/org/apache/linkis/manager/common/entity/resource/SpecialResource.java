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

package org.apache.linkis.manager.common.entity.resource;

import org.apache.linkis.manager.common.exception.ResourceWarnException;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import static org.apache.linkis.manager.common.errorcode.ManagerCommonErrorCodeSummary.NOT_RESOURCE_TYPE;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpecialResource extends Resource {
  private final Map<String, Object> resources;

  public SpecialResource(Map<String, Object> resources) {
    this.resources = resources;
  }

  public Map<String, Object> getResources() {
    return resources;
  }

  private SpecialResource specialResourceOperator(
      Resource r, BiFunction<Object, Object, Object> op) {
    if (r instanceof SpecialResource) {
      Map<String, Object> rs = ((SpecialResource) r).resources;
      Map<String, Object> resource =
          resources.entrySet().stream()
              .map(
                  entry -> {
                    Object v1 = rs.get(entry.getKey());
                    return new AbstractMap.SimpleEntry<>(
                        entry.getKey(), op.apply(entry.getValue(), v1));
                  })
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      return new SpecialResource(resource);
    } else {
      return new SpecialResource(new HashMap<>());
    }
  }

  @Override
  public Resource add(Resource r) {
    return specialResourceOperator(
        r,
        (v1, v2) -> {
          if (v1 instanceof Integer) {
            return ((int) v1) + ((int) v2);
          } else if (v1 instanceof Double) {
            return ((double) v1) + ((double) v2);
          } else if (v1 instanceof Long) {
            return ((long) v1) + ((long) v2);
          } else if (v1 instanceof Float) {
            return ((float) v1) + ((float) v2);
          } else if (v1 instanceof Short) {
            return ((short) v1) + ((short) v2);
          } else {
            throw new ResourceWarnException(
                NOT_RESOURCE_TYPE.getErrorCode(),
                MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc(), r.getClass()));
          }
        });
  }

  @Override
  public Resource minus(Resource r) {
    return specialResourceOperator(
        r,
        (v1, v2) -> {
          if (v1 instanceof Integer) {
            return ((int) v1) - ((int) v2);
          } else if (v1 instanceof Double) {
            return ((double) v1) - ((double) v2);
          } else if (v1 instanceof Long) {
            return ((long) v1) - ((long) v2);
          } else if (v1 instanceof Float) {
            return ((float) v1) - ((float) v2);
          } else if (v1 instanceof Short) {
            return ((short) v1) - ((short) v2);
          } else {
            throw new ResourceWarnException(
                NOT_RESOURCE_TYPE.getErrorCode(),
                MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc(), r.getClass()));
          }
        });
  }

  @Override
  public Resource multiplied(Resource r) {
    return specialResourceOperator(
        r,
        (v1, v2) -> {
          if (v1 instanceof Integer) {
            return ((int) v1) * ((int) v2);
          } else if (v1 instanceof Double) {
            return ((double) v1) * ((double) v2);
          } else if (v1 instanceof Long) {
            return ((long) v1) * ((long) v2);
          } else if (v1 instanceof Float) {
            return ((float) v1) * ((float) v2);
          } else if (v1 instanceof Short) {
            return ((short) v1) * ((short) v2);
          } else {
            throw new ResourceWarnException(
                NOT_RESOURCE_TYPE.getErrorCode(),
                MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc(), r.getClass()));
          }
        });
  }

  @Override
  public Resource multiplied(float rate) {
    Map<String, Object> resourceMap =
        resources.entrySet().stream()
            .map(
                entry -> {
                  Object v = entry.getValue();
                  if (v instanceof Integer) {
                    return new AbstractMap.SimpleEntry<>(
                        entry.getKey(), (int) (((Integer) v) * rate));
                  } else if (v instanceof Double) {
                    return new AbstractMap.SimpleEntry<>(entry.getKey(), ((Double) v) * rate);
                  } else if (v instanceof Long) {
                    return new AbstractMap.SimpleEntry<>(
                        entry.getKey(), (long) (((Long) v) * rate));
                  } else if (v instanceof Float) {
                    return new AbstractMap.SimpleEntry<>(entry.getKey(), ((Float) v) * rate);
                  } else if (v instanceof Short) {
                    return new AbstractMap.SimpleEntry<>(
                        entry.getKey(), (short) (((Short) v) * rate));
                  } else {
                    throw new ResourceWarnException(
                        NOT_RESOURCE_TYPE.getErrorCode(),
                        MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc(), v.getClass()));
                  }
                })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return new SpecialResource(resourceMap);
  }

  @Override
  public Resource divide(Resource r) {
    return specialResourceOperator(
        r,
        (v1, v2) -> {
          if (v1 instanceof Integer) {
            return (int) v1 / (int) v2;
          } else if (v1 instanceof Double) {
            return (double) v1 / (double) v2;
          } else if (v1 instanceof Long) {
            return (long) v1 / (long) v2;
          } else if (v1 instanceof Float) {
            return (float) v1 / (float) v2;
          } else if (v1 instanceof Short) {
            return (short) v1 / (short) v2;
          } else {
            throw new ResourceWarnException(
                NOT_RESOURCE_TYPE.getErrorCode(),
                MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc(), r.getClass()));
          }
        });
  }

  @Override
  public Resource divide(int rate) {
    Map<String, Object> map =
        resources.entrySet().stream()
            .map(
                entry -> {
                  String key = entry.getKey();
                  Object value = entry.getValue();
                  if (value instanceof Integer) {
                    int i = (int) value;
                    return new AbstractMap.SimpleEntry<>(key, i / rate);
                  } else if (value instanceof Double) {
                    double d = (double) value;
                    return new AbstractMap.SimpleEntry<>(key, d / rate);
                  } else if (value instanceof Long) {
                    long l = (long) value;
                    return new AbstractMap.SimpleEntry<>(key, l / rate);
                  } else if (value instanceof Float) {
                    float f = (float) value;
                    return new AbstractMap.SimpleEntry<>(key, f / rate);
                  } else if (value instanceof Short) {
                    short s = (short) value;
                    return new AbstractMap.SimpleEntry<>(key, (short) (s / rate));
                  } else {
                    throw new ResourceWarnException(
                        NOT_RESOURCE_TYPE.getErrorCode(),
                        MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc(), value.getClass()));
                  }
                })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    return new SpecialResource(map);
  }

  @Override
  public boolean moreThan(Resource r) {
    if (r instanceof SpecialResource) {
      SpecialResource s = (SpecialResource) r;
      Map<String, Object> rs = s.getResources();
      return resources.entrySet().stream()
          .noneMatch(
              (entry) -> {
                Object v = entry.getValue();
                Object rsV = rs.get(entry.getKey());
                if (v instanceof Integer && rsV instanceof Integer) {
                  return (int) v <= (int) rsV;
                } else if (v instanceof Double && rsV instanceof Double) {
                  return (double) v <= (double) rsV;
                } else if (v instanceof Long && rsV instanceof Long) {
                  return (long) v <= (long) rsV;
                } else if (v instanceof Float && rsV instanceof Float) {
                  return (float) v <= (float) rsV;
                } else if (v instanceof Short && rsV instanceof Short) {
                  return (short) v <= (short) rsV;
                } else {
                  throw new ResourceWarnException(
                      NOT_RESOURCE_TYPE.getErrorCode(),
                      MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc(), v.getClass()));
                }
              });
    } else {
      return true;
    }
  }

  @Override
  public boolean caseMore(Resource r) {
    if (r instanceof SpecialResource) {
      SpecialResource s = (SpecialResource) r;
      Map<String, Object> rs = s.getResources();
      return resources.entrySet().stream()
          .anyMatch(
              (entry) -> {
                Object v = entry.getValue();
                Object rsV = rs.get(entry.getKey());
                if (v instanceof Integer && rsV instanceof Integer) {
                  return (int) v > (int) rsV;
                } else if (v instanceof Double && rsV instanceof Double) {
                  return (double) v > (double) rsV;
                } else if (v instanceof Long && rsV instanceof Long) {
                  return (long) v > (long) rsV;
                } else if (v instanceof Float && rsV instanceof Float) {
                  return (float) v > (float) rsV;
                } else if (v instanceof Short && rsV instanceof Short) {
                  return (short) v > (short) rsV;
                } else {
                  throw new ResourceWarnException(
                      NOT_RESOURCE_TYPE.getErrorCode(),
                      MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc(), v.getClass()));
                }
              });
    } else {
      return true;
    }
  }

  @Override
  public boolean equalsTo(Resource r) {
    if (r instanceof SpecialResource) {
      SpecialResource s = (SpecialResource) r;
      Map<String, Object> rs = s.getResources();
      return resources.entrySet().stream()
          .noneMatch(
              (entry) -> {
                Object v = entry.getValue();
                Object rsV = rs.get(entry.getKey());
                if (v instanceof Integer && rsV instanceof Integer) {
                  return (int) v != (int) rsV;
                } else if (v instanceof Double && rsV instanceof Double) {
                  return !BigDecimal.valueOf((double) v).equals(BigDecimal.valueOf((double) rsV));
                } else if (v instanceof Long && rsV instanceof Long) {
                  return (long) v != (long) rsV;
                } else if (v instanceof Float && rsV instanceof Float) {
                  return !BigDecimal.valueOf((float) v).equals(BigDecimal.valueOf((float) rsV));
                } else if (v instanceof Short && rsV instanceof Short) {
                  return (short) v != (short) rsV;
                } else {
                  throw new ResourceWarnException(
                      NOT_RESOURCE_TYPE.getErrorCode(),
                      MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc(), v.getClass()));
                }
              });
    } else {
      return true;
    }
  }

  @Override
  public boolean notLess(Resource r) {
    if (r instanceof SpecialResource) {
      SpecialResource s = (SpecialResource) r;
      Map<String, Object> rs = s.getResources();
      return resources.entrySet().stream()
          .noneMatch(
              (entry) -> {
                Object v = entry.getValue();
                Object rsV = rs.get(entry.getKey());
                if (v instanceof Integer && rsV instanceof Integer) {
                  return (int) v < (int) rsV;
                } else if (v instanceof Double && rsV instanceof Double) {
                  return (double) v < (double) rsV;
                } else if (v instanceof Long && rsV instanceof Long) {
                  return (long) v < (long) rsV;
                } else if (v instanceof Float && rsV instanceof Float) {
                  return (float) v < (float) rsV;
                } else if (v instanceof Short && rsV instanceof Short) {
                  return (short) v < (short) rsV;
                } else {
                  throw new ResourceWarnException(
                      NOT_RESOURCE_TYPE.getErrorCode(),
                      MessageFormat.format(NOT_RESOURCE_TYPE.getErrorDesc(), v.getClass()));
                }
              });
    } else {
      return true;
    }
  }

  @Override
  public boolean less(Resource r) {
    return !notLess(r);
  }

  @Override
  public int compare(Resource r) {
    if (this.moreThan(r)) {
      return 1;
    } else if (this.less(r)) {
      return -1;
    } else {
      return 0;
    }
  }

  @Override
  public String toJson() {
    return String.format("Special:%s", resources);
  }
}
