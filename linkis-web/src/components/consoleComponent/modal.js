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


/**
 * 脚本任务
 */
export class Script {
  /**
     * Constructor(构造器)
     * @param {*} option
     */
  constructor(option) {
    this.id = option.id;
    this.title = option.title;
    // log(日志)
    this.log = option.log || {};
    this.logLine = option.logLine || 1;
    // history(历史)
    this.history = [];
    // schedule(进度)
    this.progress = {
      current: null,
      progressInfo: [],
      waitingSize: null,
      costTime: null,
    };
    // step(步骤)
    this.steps = [];
    // operation result(运行结果)
    this.result = null;
    // Record the storage path of the result set(记录结果集的存储路径)
    this.resultList = null;
    // current operating state(当前的运行状态)
    this.status = option.status ? option.status : 'Inited';
    // script view state(script视图状态)
    this.scriptViewState = {};
  }
}
