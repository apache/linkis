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

package org.apache.linkis.cs.listener.callback;

import org.apache.linkis.cs.listener.callback.imp.ContextKeyValueBean;

import java.util.ArrayList;

public interface CallbackEngine {

  // 某个client发送心跳的时候，
  // 这个callbackengine就需要将这个client已经注册的所有cskey更改的内容进行通知给client，
  // 应该是返回一个数组的形式或者为空
  ArrayList<ContextKeyValueBean> getListenerCallback(String source);
}
