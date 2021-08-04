/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/**
 * 脚本任务
 */
export class Script {
  /**
     * 构造器
     * @param {*} option
     */
  constructor(option) {
    this.id = option.id;
    this.title = option.title;
    // 日志
    this.log = option.log || {};
    this.logLine = option.logLine || 1;
    // 历史
    this.history = [];
    // 进度
    this.progress = {
      current: null,
      progressInfo: [],
      waitingSize: null,
      costTime: null,
    };
    // 步骤
    this.steps = [];
    // 运行结果
    this.result = null;
    // 记录结果集的存储路径
    this.resultList = null;
    // 当前的运行状态
    this.status = option.status ? option.status : 'Inited';
    // script视图状态
    this.scriptViewState = {};
  }
}
