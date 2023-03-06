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


// a web app only has a socket
import Socket from '@/common/service/socket';
const manager = {
  timer: null,
  socket: null,
  interval: 100,
  retryInterval: 1000,
  retryTimer: null,
  retryCounter: 3,
  errCode: 0,
  delayCounter: 0,
};
const module = {
  name: 'WebSocket',
  events: [],
  dispatchs: ['Workbench:socket'],
  methods: {
    init() {
      if (manager.socket) {
        if (manager.socket.readyState === 3) {
          manager.retryCounter = 3;
          return module.methods.reconnect();
        }
        return;
      }
      manager.socket = new Socket({
        url: `ws://${process.env.VUE_APP_HOST || window.location.host}${process.env.VUE_APP_MN_CONFIG_SOCKET}`,
        errHandler: () => {
          // window.$Wa.log(JSON.stringify(e));
        },
      });
      manager.socket.on('inconnect', () => {
        module.methods.downgrade();
        window.console.warn('socket连接失败，后续请求将使用http');
      });
      manager.socket.on('close', (e) => {
        module.methods.clearTimer();
        manager.errCode = e.code;
        if (e.code != 1001 && e.code != 1005) {
          manager.retryTimer = setTimeout(() => {
            module.methods.reconnect(null, e.code);
          }, manager.retryInterval);
        } else {
          manager.retryCounter = 3;
        }
      });
      manager.socket.on('data', (data) => {
        module.dispatch('Workbench:socket', {
          type: 'data',
          data,
        });
      });
      manager.socket.on('dataError', (data) => {
        module.dispatch('Workbench:socket', {
          type: 'dataError',
          data,
        });
      });
    },
    close() {
      manager.socket && manager.socket.close(1000);
    },
    send(data) {
      // handle socket disconnect while
      if (!manager.socket) {
        module.methods.downgrade(data);
        // handle idle, socket will not connect immediately insteads while sending data
      } else if (manager.socket.readyState === 0) {
        module.methods.delaySend(data);
      } else if (manager.socket.readyState === 1) {
        manager.socket.send(data);
      } else if (manager.errCode === 1001) {
        module.methods.reconnect(data, 1001);
        module.methods.reSend(data);
      } else {
        window.console.warn(`socket readystate${manager.socket.readyState}：本次请求将使用http`);
        module.methods.downgrade(data);
      }
    },
    delaySend(data) {
      if (manager.delayCounter > 10) {
        manager.delayCounter = 0;
        window.console.warn(`socket readystate重试多次不为open：本次请求将使用http`);
        return module.methods.downgrade(data);
      }
      module.methods.reSend(data);
      manager.delayCounter++;
    },
    downgrade(data) {
      if (data) {
        module.methods.clearResendTimer();
      }
      module.methods.clearTimer();
      manager.retryCounter = 1;
      module.dispatch('Workbench:socket', {
        type: 'downgrade',
        data,
      });
    },
    reconnect(data = null, code) {
      module.methods.clearTimer();
      if (--manager.retryCounter > 0) {
        manager.socket.reconnect(data);
      } else {
        module.methods.downgrade(data);
        window.console.warn(`错误码${code}：本次请求将使用http`);
      }
    },
    reSend(data) {
      module.methods.clearResendTimer();
      manager.timer = setTimeout(() => {
        module.methods.send(data);
      }, manager.interval);
    },
    clearTimer() {
      clearTimeout(manager.retryTimer);
    },
    clearResendTimer() {
      clearTimeout(manager.timer);
    },
  },
}
;
// Close the socket first and register(先关socket全局注册)
// export default module;
export default { name: 'WebSocket' }
