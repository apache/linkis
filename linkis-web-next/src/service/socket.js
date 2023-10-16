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


import { Eventbus } from '@/common/helper/eventbus';
/**
 *
 * @class Socket
 * @extends {Eventbus}
 */
class Socket extends Eventbus {
  /**
     *Creates an instance of Socket.
     * @param {*} options
     * @memberof Socket
     */
  constructor(options) {
    super();
    this.options = options;
    this.errHandler = options.errHandler;
    this.init();
  }
  /**
     *get current state
     * @readonly
     * @memberof Socket
     */
  get readyState() {
    return this.ws.readyState;
  }
  /**
     * init event
     * @memberof Socket
     */
  init() {
    try {
      this.ws = new WebSocket(this.options.url);
      this.ws.debug = true;
    } catch (e) {
      this.emit('inconnect', {
        type: 'inconnect',
        message: 'socket连接失败',
      });
    }
    this._onError();
    this._onClose();
    this._onOpen();
    this._onMessage();
  }
  /**
     *
     * @param {*} options
     * @memberof Socket
     */
  send(options) {
    if (this._validState('send')) {
      this.ws.send(JSON.stringify(options));
    }
  }
  /**
     * @param {*} code
     * @memberof Socket
     */
  close(code) {
    this.ws.close(code);
  }
  /**
     * @return {this}
     * @memberof Socket
     */
  reconnect() {
    this.init();
    return this;
  }
  /**
     * onMessage
     * @memberof Socket
     */
  _onMessage() {
    let instance = this;
    this.ws.onmessage = (event = {}) => {
      if (event.data) {
        let msg = JSON.parse(event.data);
        let result = instance._validMessage(msg);
        if(result) {
          instance.emit('data', result);
        }
      } else {
        instance.errHandler && instance.errHandler({
          type: 'message',
          msg: '接收到无效数据',
        });
      }
    };
  }

  /**
     * onOpen
     * @memberof Socket
     */
  _onOpen() {
    this.ws.onopen = () => {
      this.connected = true;
      this.emit('open', {
        msg: '连接成功',
      });
    };
  }
  /**
     *
     * @param {*} e
     * @memberof Socket
     */
  _onError() {
    this.ws.onerror = (e) => {
      let data = {
        type: 'socket listening error',
        msg: e,
      };
      this.errHandler && this.errHandler(data);
    };
  }
  /**
     * onClose
     * @memberof Socket
     */
  _onClose() {
    this.ws.onclose = (e) => {
      let code = e.code;
      let reason = e.reason;
      let data = {
        msg: 'socket close tips',
        reason,
        code,
      };
      this.connected = false;
      this.errHandler && this.errHandler(data);
      this.emit('close', data);
    };
  }
  /**
     * @param {*} type
     * @memberof Socket
     * @return {Boolean} valid
     */
  _validState(type) {
    let state = this.ws.readyState;
    let stateMap = {
      0: '正在连接',
      1: '成功',
      2: '正在关闭',
      3: '连接已经关闭或打开连接失败',
    };
    if (state === 2 || state === 3) {
      let error = {
        type,
        msg: stateMap[state],
      };
      this.errHandler && this.errHandler(error);
    }
    return state === 1;
  }
  /**
     * @param {*} data
     * @memberof Socket
     * @return {*} data
     */
  _validMessage(data = {}) {
    let msgData = data.data;
    let method = data.method;
    // Ignore duplicate push data without log information(忽略无日志信息的重复推送数据)
    let notEmpty = msgData && Array.isArray(msgData.log) ? msgData.log.some((it) => it.length > 0) : msgData.log;
    if (/\/log$/.test(method) && !notEmpty ) {
      return
    }
    if (!(data instanceof Object)) return;
    if (data.status !== 0) {
      this.emit('dataError', {
        type: 'network',
        message: data.message,
        data,
      });
    }
    return data;
  }
}
export default Socket;
