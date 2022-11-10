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

package org.apache.linkis.orchestrator.computation.utils;

import org.apache.linkis.orchestrator.plans.physical.ExecTask;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TaskTreeUtil {

  public static <T> List<T> getAllTask(List<Object> list, Class<T> classT)
      throws IllegalArgumentException {
    if (null == classT) {
      throw new IllegalArgumentException("classT cannot be null.");
    }

    List<T> rsList = new ArrayList<T>();

    for (Object o : list) {
      if (classT.isInstance(o)) {
        rsList.add((T) o);
      }
    }
    return rsList;
  }

  public static <T> List<T> getAllTaskRecursive(ExecTask root, Class<T> classT)
      throws IllegalArgumentException {
    if (null == root || null == classT) {
      throw new IllegalArgumentException("classT cannot be null.");
    }

    List<T> rsList = new ArrayList<>();
    Function<Object, Integer> visitFunc =
        (o) -> {
          if (classT.isInstance(o)) {
            rsList.add(classT.cast(o));
          }
          return 0;
        };

    traverseTask(root, visitFunc);
    return rsList;
  }

  private static void traverseTask(ExecTask root, Function<Object, Integer> visitFunc) {
    if (null != root) {
      visitFunc.apply(root);
      if (null != root.getChildren()) {
        for (ExecTask node : root.getChildren()) {
          traverseTask(node, visitFunc);
        }
      }
    }
  }
}
