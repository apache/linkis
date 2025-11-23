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


import api from '@/common/service/api';
import router from '@/router';
import storage from '@/common/helper/storage';
import Vue from 'vue';
import axios from 'axios';
import { Message } from 'iview';
import i18n from '@/common/i18n'

/**
 * Provide script running related methods, including execution methods, status polling, log reception, and obtaining results, etc.(提供脚本运行相关方法，包括执行方法，状态轮询，日志接收，获取结果等)
 * * 1. By default, socket communication is used. If the socket connection fails, the http method is used.(默认使用socket方式通信，若socket连接失败则使用http方式)
 * * 2. Click Execute to call the start method, and enter the executing state after receiving the taskID(点击执行调用start方法，收到taskID后进入执行中状态)
 * * 3.The task in execution closes the script tab, and needs to restore the state when reopening(执行中任务关闭脚本tab，重新打开时需要恢复状态)
 * * 4.After the task socket is downgraded to http, call queryStatus to poll the progress, log(执行中任务socket降级http之后调用queryStatus轮询进度，日志)
 * ! 5.This method is a public method that is reused during script execution and workflow node execution.(本方法是script执行、工作流节点执行时复用的公共方法)
 * @param { Object } data 待执行脚本信息
 */
function Execute(data) {
  this.executeTimout = null;
  this.statusTimeout = null;
  this.diagnosisTimeout = null;
  this.diagnosisTimer = 300;
  this.beginTime = Date.now();
  this.progress = 0;
  this.id = null;
  this.status = null;
  // Only used by the /api/jobhistory/${id}/get interface(仅/api/jobhistory/${id}/get接口使用)
  this.taskID = null;
  this.postType = process.env.NODE_ENV === 'sandbox' ? 'http' : (data.data.postType || 'socket');
  delete data.data.postType;
  this.data = data;
  this.executionCode = null;
  this.model = 'normal';
  this.fromLine = data.fromLine || 1;
  this.runType = data.data.runType;
  this.event = new Vue();
  this.run = false;
  // Store the relevant information of the result set, use the log and result path inside(存储结果集的相关信息，用到里面的日志和结果路径)
  this.resultsetInfo = null;
  // Stores all information in the result set directory, which can be used to get a single result set(存储结果集目录下的所有信息，可用于拿到单个结果集)
  this.resultList = [];
  // the path of the current result set(当前结果集的path)
  this.currentResultPath = '';
  this.isDiagnosisFailed = false;
  // Used to control the url to get the result set(用来控制获取结果集的url)
  this.getResultUrl = data.getResultUrl || 'filesystem',
  this.on('execute', () => {
    this.run = true;
    // The comment is reserved for when the request is initiated, when the first interface is not returned for a long time(注释是留作发起请求时，长时间没返回第一个接口时)
    // timeoutCheck(this);
  });
  this.on('execute:queryState', () => {
    this.queryStatus({ isKill: false });
    this.queryProgress();
  });
  this.on('stateEnd', () => {
    this.getResultPath();
  });
  this.on('getResultList', () => {
    this.getResultList();
  });
  this.on('getResult', () => {
    this.getFirstResult();
  });
  this.on('stop', () => {
    clearTimeout(this.executeTimout);
    clearTimeout(this.statusTimeout);
    this.run = false;
  });
  this.on('kill', () => {
    if (this.postType === 'http') {
      this.queryStatus({ isKill: true });
    }
  });
  // data is the return data of the interface(data是接口的返回数据)
  // execute.data is the request data sent from the foreground to the background(execute.data是前台发送至后台的请求数据)
  this.on('data', ({ data, execute }) => {
    if (execute.postType !== 'socket') return;
    // Here, we judge the situation that the first interface execute of websocket will directly return errorMsg(这里对websocket第一个接口execute直接会返回errorMsg的情况进行判断)
    if (Object.prototype.hasOwnProperty.call(data.data, 'errorMsg')) {
      execute.trigger('stop');
      execute.trigger('error');
      return;
    }
    const method = execute.data.method;
    if (!execute.taskID) {
      const socketTag = data.data && data.data.websocketTag;
      if (execute.data.websocketTag === socketTag && data.method === method) {
        // clearTimeout(execute.executeTimout);
        timeoutCheck(execute);
        api.fetch('/jobhistory/list', {
          pageSize: 100,
          status: 'Running,Inited,Scheduled',
        }, 'get').then((rst) => {
          execute.trigger('updateResource', rst.tasks.length);
        });
        deconstructExecute(execute, data.data);
        this.trigger('steps', 'Inited');
      }
    } else {
      const id = data.data && data.data.taskID;
      let prefix = method.slice(0, method.lastIndexOf('/') + 1);
      if (execute.taskID !== id) {
        // For the case where the expired data is not updated when the temporary script is right-clicked multiple times(针对临时脚本多次右键点击的，过期数据不更新的情况)
        if (data.data.status) {
          return execute.trigger('updateExpireHistory', data.data);
        }
        return;
      }
      if (data.method === prefix + `${execute.id}/status`) {
        reawakening(execute);
        deconstructStatus(execute, data.data);
      } else if (data.method === prefix + `${execute.id}/progressWithResource`) {
        reawakening(execute);
        execute.trigger('progress', data.data);
      } else if (data.method === prefix + `${execute.id}/log`) {
        reawakening(execute);
        execute.trigger('log', data.data.log);
      } else if (data.method === prefix + `${execute.id}/waitingSize`) {
        const log = `**Waiting queue：${i18n.t('message.common.execute.waitingQueue', { num: data.data.waitingSize })}`;
        execute.trigger('log', log);
        execute.trigger('progress', { progress: 0, progressInfo: [], waitingSize: data.data.waitingSize });
        clearTimeout(execute.executeTimout);
        clearTimeout(execute.statusTimeout);
      } else if (data.method === prefix + `${execute.id}/runtimeTuning`) {
        execute.trigger('diagnosis', data.data.diagnosticInfo);
      } else {
        if (data.data.status) {
          execute.trigger('updateExpireHistory', data.data);
        }
        clearTimeout(execute.executeTimout);
        clearTimeout(execute.statusTimeout);
      }
    }
  });
  this.on('downgrade', ({ execute }) => {
    execute.postType = 'http';
    // When the script is running, if the socket is downgraded to http, the http interface is used to poll the progress(脚本运行中，由socket降级成http则使用http接口轮询进度)
    // There are two possibilities when the websocket is downgraded, 1. The task has been started and the connection is disconnected before returning the id, 2. One is that the connection is lost before starting(当websocket降级时有两种可能，1，，任务已经启动，在返回id前就断了连接， 2.一种是在启动前就失去连接)
    if (execute.run) {
      if (this.id && this.taskID) {
        execute.queryStatus({isKill: false});
      } else {
        // run the task again(重新跑任务)
        console.warn('[websocket降级没有获取到taskID]');
        // this.execute(); // Do not actively help users start tasks(不主动帮用户启动任务)
      }
    }
  });
  this.on('dataError', ({ data, execute }) => {
    execute.run = false;
    if (data.data.status === -1) {
      router.push('/login');
      Message.warning(data.message);
    } else {
      this.trigger('notice', {
        type: 'warning',
        msg: data.message,
        autoJoin: false,
      });
    }
  });
}

Execute.prototype.start = function() {
  setTimeout(() => {
    this.execute();
  }, 0);
};

Execute.prototype.restore = function({ execID, taskID }) {
  this.id = execID;
  this.taskID = taskID;
  this.run = true;
  this.postType = 'http';
  this.trigger('execute:queryState');
};

Execute.prototype.halfExecute = function({ execID, taskID }) {
  this.id = execID;
  this.taskID = taskID;
  this.run = true;
  this.postType = 'http';
  this.trigger('execute:queryState');
};

Execute.prototype.on = function(name, cb) {
  this.event.$on(name, cb);
};
Execute.prototype.off = function() {
  this.event.$off();
};
Execute.prototype.once = function(name, cb) {
  this.event.$once(name, cb);
};
Execute.prototype.trigger = function(name, data) {
  this.event.$emit(name, data);
};

Execute.prototype.execute = function() {
  this.trigger('execute');
  if (this.postType === 'http') {
    return this.httpExecute();
  }
  this.trigger('WebSocket:send', this.data);
};

Execute.prototype.httpExecute = function() {
  this.trigger('execute');
  const method = this.data.method.slice(this.data.method.indexOf('entrance'), this.data.method.length);
  api.fetch(method, this.data.data)
    .then((ret) => {
      deconstructExecute(this, ret);
      this.trigger('execute:queryState');
      this.trigger('steps', 'Inited');
    })
    .catch(() => {
      this.trigger('stop');
      this.trigger('error', 'execute');
    });
};

Execute.prototype.outer = function(outerUrl, ret) {
  axios.put(
    outerUrl, {
      need_repair: true,
      task_id: ret.id,
    }, {
      withCredentials: true,
    }
  );
};

Execute.prototype.queryStatus = function({ isKill }) {
  const requestStatus = (ret) => {
    if (isKill) {
      deconstructStatusIfKill(this, ret);
    } else {
      deconstructStatus(this, ret);
    }
  };
  api.fetch(`/entrance/${this.id}/status`, {taskID: this.taskID}, 'get')
    .then((ret) => {
      this.status = ret.status;
      requestStatus(ret);
    })
    .catch(() => {
      requestStatus({
        status: this.status
      });
    });
};

Execute.prototype.queryProgress = function() {
  api.fetch(`/entrance/${this.id}/progressWithResource`, 'get')
    .then((rst) => {
      this.trigger('progress', { progress: rst.progress, progressInfo: rst.progressInfo });
    });
};

Execute.prototype.queryLog = function() {
  return api.fetch(`/entrance/${this.id}/log`, {
    fromLine: this.fromLine,
    size: -1,
  }, 'get')
    .then((rst) => {
      this.fromLine = rst.fromLine;
      this.trigger('log', rst.log);
      return Promise.resolve();
    }).catch(() => {
      return Promise.resolve();
    });
};

Execute.prototype.getResultPath = function() {
  this.trigger('steps', 'ResultLoading');
  // api执行时的路径不一样
  const taskUrl = this.getResultUrl !== 'filesystem' ? this.getResultUrl : 'jobhistory';
  api.fetch(`/${taskUrl}/${this.taskID}/get`, 'get')
    .then((rst) => {
      if (rst.task) {
        this.resultsetInfo = rst.task;
        this.trigger('querySuccess', {
          type: '执行',
          task: rst.task,
        });
        this.trigger('getResultList');
        this.updateLastHistory(rst.task);
      } else {
        throw new Error('task null');
      }
    })
    .catch((err) => {
      this.trigger('steps', 'FailedToGetResultPath');
      logError(err, this);
    });
};

Execute.prototype.getResultList = function() {
  if (this.resultsetInfo && this.resultsetInfo.resultLocation) {
    let params = {
      path: `${this.resultsetInfo.resultLocation}`,
    }
    // If it is api execution, you need to bring taskId(如果是api执行需要带上taskId)
    if (this.getResultUrl !== 'filesystem') {
      params.taskId = this.taskID
    }
    api.fetch(
      `/${this.getResultUrl}/getDirFileTrees`, params,
      'get'
    )
      .then((rst) => {
        // The order of the result set in the background is sorted by string according to the name of the result set. When displaying, there will be a problem that the result set cannot be matched, so add sorting(后台的结果集顺序是根据结果集名称按字符串排序的，展示时会出现结果集对应不上的问题，所以加上排序)
        if(rst.dirFileTrees && rst.dirFileTrees.children) {
          this.resultList = rst.dirFileTrees.children.sort((a, b) => {
            const slice = (name) => {
              return Number(name.slice(1, name.lastIndexOf('.')));
            }
            return slice(a.name) - slice(b.name);
          });
        }
        this.trigger('getResult');
      })
      .catch((err) => {
        this.trigger('steps', 'FailedToGetResultList');
        logError(err, this);
      });
  } else {
    this.trigger('notice', {
      type: 'error',
      msg: i18n.t('message.common.execute.error.getResultList'),
      autoJoin: true,
    });
  }
};

Execute.prototype.getFirstResult = function() {
  if (this.resultList.length < 1 ) {
    const log = '**result tips: empty!';
    this.trigger('log', log);
    this.trigger('steps', 'Completed');
    this.run = false;
  } else {  // Get the first result set(获取第一个结果集)
    this.currentResultPath = this.resultList[0].path;
    // The log path needs to be provided to download the log(需要提供日志路径，用于下载日志)
    this.trigger('history', {
      execID: this.id,
      logPath: this.resultsetInfo.logPath,
      taskID: this.taskID,
      status: this.status,
    });
    let params = {
      path: this.currentResultPath,
      pageSize,
    }
    // If it is api execution, you need to bring taskId(如果是api执行需要带上taskId)
    if (this.getResultUrl !== 'filesystem') {
      params.taskId = this.taskID
    }
    const url = `/${this.getResultUrl}/openFile`;
    const pageSize = 5000;
    api.fetch(url, params, 'get')
      .then((rst) => {
        this.trigger('result', rst);
        const log = `**result tips: ${i18n.t('message.common.execute.success.getResultList')}`;
        this.trigger('log', log);
        this.trigger('steps', 'Completed');
        this.run = false;
      })
      .catch((err) => {
        this.trigger('steps', 'FailedToGetResultFirst');
        logError(err, this);
      });
  }
};

// Get error reason and update history(获取错误原因，并更新历史)
Execute.prototype.updateLastHistory = function(option, cb) {
  if (option) {
    return this.trigger('history', {
      taskID: option.taskID,
      execID: '',
      createDate: option.createdTime,
      runningTime: option.costTime,
      status: option.status,
      failedReason: '',
    });
  }
  // The path when the api is executed is different(api执行时的路径不一样)
  const taskUrl = this.getResultUrl !== 'filesystem' ? this.getResultUrl : 'jobhistory';
  api.fetch(`/${taskUrl}/${this.taskID}/get`, 'get')
    .then((res) => {
      const task = res.task;
      if (cb) {
        cb(task);
      }
      this.executionCode = task.executionCode;
      this.trigger('history', {
        taskID: task.taskID,
        execID: '',
        createDate: task.createdTime,
        runningTime: task.costTime,
        // This is changed to the status of execute because the state may not be flipped in the case of a large result set in the database, but the state pushed by the websocket is correct(这里改成使用execute的status是因为数据库中在大结果集的情况下可能会发生状态不翻转的情况，但websocket推送的状态是对的)
        status: this.status,
        failedReason: task.errCode && task.errDesc ? task.errCode + task.errDesc : task.errCode || task.errDesc || ''
      });
      if (task.progress === 1) {
        this.trigger('costTime', task.costTime);
      }
    }).catch(() => {
      this.trigger('history', {
        taskID: this.taskID,
        status: this.status,
        isPartialUpdate: true,
      });
    });
};

/**
 * When killing, go to poll to get the canceled status(kill的时候去轮询获取cancelled状态)
 * @param {*} execute
 * @param {*} ret
 */
function deconstructStatusIfKill(execute, ret) {
  if (ret.status !== 'Cancelled') {
    setTimeout(() => {
      execute.queryStatus({ isKill: true });
    }, 5000);
  } else {
    const msg = '查询已被取消';
    queryException(execute, 'warning', msg);
  }
}

/**
 * poll status(轮询状态)
 * @param {*} execute
 * @param {*} ret
 */
function deconstructStatus(execute, ret) {
  clearTimeout(execute.executeTimout);
  execute.status = ret.status;
  execute.trigger('steps', ret.status);
  execute.trigger('status', ret.status);
  switch (ret.status) {
    case 'Inited': case 'Scheduled': case 'Running':
      // Update history when state changes(在状态发生改变的时候更新历史)
      execute.updateLastHistory();
      if (execute.postType !== 'socket') {
        // Send a request every 5 seconds(5秒发送一次请求)
        if (!execute.run) return;
        setTimeout(() => {
          execute.queryStatus({ isKill: false });
          execute.queryProgress();
          execute.queryLog();
        }, 5000);
      }
      break;
    case 'Succeed':
      if ((!execute.fromLine || execute.fromLine === 1) && execute.postType !== 'socket') {
        execute.queryLog().then(() => {
          whenSuccess(execute);
        });
        break;
      }
      whenSuccess(execute);
      break;
    default:
      if ((!execute.fromLine || execute.fromLine === 1) && execute.postType !== 'socket') {
        execute.queryLog().then(() => {
          whenException(execute, ret);
        });
        break;
      }
      whenException(execute, ret);
      break;
  }
}

/**
 * Logic to execute when the status is successful(当状态为成功时执行的逻辑)
 * @param {*} execute
 */
function whenSuccess(execute) {
  if (execute.runType !== 'pipeline') {
    // stateEnd needs to obtain the result set, and the history will be updated when the result set is obtained(stateEnd是需要获取结果集的，获取结果集的同时会更新历史)
    execute.trigger('stateEnd');
    const log = `**result tips: ${i18n.t('message.common.execute.success.stateEnd')}`;
    execute.trigger('log', log);
  } else {
    // Import and export do not need to request the result set, but need to update the history, otherwise there will be a problem that the state is not flipped.(导入导出不需要请求结果集，但需要更新历史，否则会出现状态未翻转的问题。)
    const log = `**result tips: ${i18n.t('message.common.execute.error.noResultList')}`;
    execute.trigger('log', log);
    execute.trigger('steps', 'Completed');
    execute.updateLastHistory('', (task) => {
      execute.trigger('querySuccess', {
        type: '导入/导出',
        task,
      });
    });
  }
  execute.trigger('stop');
}

/**
 * Logic to execute when the state is abnormal(当状态为异常状态时执行的逻辑)
 * @param {*} execute
 * @param {*} ret
 */
function whenException(execute, ret) {
  if (ret.status === 'Failed') {
    const msg = i18n.t('message.common.execute.error.failed');
    queryException(execute, 'error', msg);
  }
  if (ret.status === 'Cancelled') {
    const msg = i18n.t('message.common.execute.error.canceled');
    queryException(execute, 'warning', msg);
  }
  if (ret.status === 'Timeout') {
    const msg = i18n.t('message.common.execute.error.executeTimeout');
    queryException(execute, 'error', msg);
    clearInterval(execute.diagnosisTimeout);
  }
}

/**
 * inner helper
 * @param {*} execute
 * @param {*} ret
 */
function deconstructExecute(execute, ret) {
  if (Object.prototype.hasOwnProperty.call(ret, 'errorMsg')) {
    execute.updateLastHistory();
    execute.trigger('stop');
    execute.trigger('error');
    execute.trigger('notice', {
      type: 'error',
      msg: ret.errorMsg.desc,
      autoJoin: false,
    });
  } else {
    execute.id = ret.execID;
    execute.taskID = ret.taskID;
    const flag = ['qmlsql', 'qmlpy'].includes(execute.runType);
    const outerUrl = storage.get('outerUrl');
    if (flag && outerUrl) {
      execute.outer(outerUrl, ret);
    }
    execute.trigger('history', {
      taskID: ret.taskID,
      execID: ret.execID,
      createDate: execute.beginTime,
      runningTime: 0,
      status: 'Inited',
      failedReason: '',
    });
    setModelAndGetCode(execute, execute.data.method).then((code) => {
      execute.trigger('sendStart', code);
    });
  }
}

/**
 * More than one minute does not return,then terminate the request.
 * @param {*} execute
 */
function timeoutCheck(execute) {
  const timeout = 1000 * 60;
  clearTimeout(execute.executeTimout);
  execute.executeTimout = setTimeout(() => {
    execute.trigger('stop');
    execute.trigger('error');
    execute.trigger('notice', {
      type: 'error',
      msg: i18n.t('message.common.execute.error.executeTimeout'),
      autoJoin: true,
    });
  }, timeout);
}

/**
 * When the websocket request has not returned for more than 1 minute, send a status request to wake up again.(当websocket请求超过1分钟未返回时，发送一个status请求重新唤醒.)
 * @param {*} execute
 */
function reawakening(execute) {
  const timeout = 1000 * 60;
  clearTimeout(execute.statusTimeout);
  execute.statusTimeout = setTimeout(() => {
    // http方式
    // api.fetch(`/entrance/${execute.id}/status`, 'get')
    //     .then((ret) => {
    //         execute.status = ret.status;
    //     });
    // websocket方式
    const data = {
      method: `/api/rest_j/v1/entrance/${execute.id}/status`,
    };
    execute.trigger('WebSocket:send', data);
  }, timeout);
}

/**
 *
 * @param {*} err
 * @param {*} that
 */
function logError(err, that) {
  that.run = false;
  const notResultLog = `**result tips: ${i18n.t('message.common.execute.error.noResultList')}`;
  const errorLog = `**result tips: ${err},${i18n.t('message.common.execute.error.errorLog')}`;
  const notResult = err.message === `Cannot read property 'children' of null`;
  const log = notResult ? notResultLog : errorLog;
  that.trigger('log', log);
  if (notResult) {
    that.trigger('steps', 'Completed');
  }
}

/**
 * Action when query is abnormal(查询异常时的操作)
 * @param {*} execute the current object(execute 当前的对象)
 * @param {*} type notice的类型(type notice的类型)
 * @param {*} The prompt text of msg notice(msg notice的提示文本)
 */
function queryException(execute, type, msg) {
  clearInterval(execute.diagnosisTimeout);
  execute.updateLastHistory();
  execute.trigger('stop');
  execute.trigger('error');
  execute.trigger('notice', {
    type,
    msg,
    autoJoin: true,
  });
}

/**
 * When webscoket is in background mode, when receiving the execute interface, the get request or the script content spliced ​​in the background is called;(webscoket为background模式时，在接收execute接口时调用get请求或者后台拼接的脚本内容；)
 * @param {*} execute
 * @param {*} method
 * @return {*}
 */
function setModelAndGetCode(execute, method) {
  return new Promise((resolve) => {
    const model = method.slice(method.lastIndexOf('/') + 1, method.length);
    if (model === 'backgroundservice') {
      execute.model = 'background';
      api.fetch(`/jobhistory/${execute.taskID}/get`, 'get').then((res) => {
        execute.executionCode = res.task.executionCode;
        resolve(execute.executionCode);
      }).catch(() => {
        execute.executionCode = null;
      });
    } else if (model === 'execute') {
      execute.model = 'normal';
      resolve('');
    }
  });
}

export default Execute;
