<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->
<template>
  <div>
    <div
      ref="terminal" 
      class="terminal-container"
      :style="{ height: terminalHeight + 'px' }"
    >
      <Spin
        v-show="loading"
        size="large"
        fix/>
    </div>
  </div>
  
</template>

<script>
import { Terminal } from 'xterm'
import { FitAddon } from 'xterm-addon-fit'
import 'xterm/css/xterm.css'

export default {
  name: 'TerminalLog',
  props: {
    logs: {
      type: String,
      default: ''
    },
    scriptViewState: {
      type: Object,
      default: () => {
        return {};
      },
    },
    loading: {
      type: Boolean,
      default: false
    }
  },

  data() {
    return {
      terminal: null,
      fitAddon: null,
      terminalHeight: 400,
    }
  },

  watch: {
    logs(newVal) {
      if (this.terminal && newVal) {
        this.terminal.clear();
        this.terminal.write(newVal)
        setTimeout(() => {
          this.terminal.scrollToTop();
        }, 0)
        
      }
    },
    scriptViewState: {
      handler(newVal) {
        this.terminalHeight = newVal.bottomContentHeight
        this.$nextTick(() => {
          this.fitAddon?.fit()
        })
      },
      deep: true,
      immediate: true,
    }
  },

  mounted() {
    this.initTerminal()
    window.addEventListener('resize', this.onResize)
  },

  beforeDestroy() {
    window.removeEventListener('resize', this.onResize)
    if (this.terminal) {
      this.terminal.dispose()
    }
  },

  methods: {
    copyToClipboard(text) {
      try {
        // 优先使用 navigator.clipboard
        if (navigator.clipboard && navigator.clipboard.writeText) {
          return navigator.clipboard.writeText(text);
        }
    
        // 降级使用 execCommand
        const textarea = document.createElement('textarea');
        textarea.value = text;
        textarea.style.position = 'fixed';
        textarea.style.left = '-9999px';
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
        return Promise.resolve();
      } catch (err) {
        return Promise.reject(err);
      }
    },
    initTerminal() {
      this.terminal = new Terminal({
        cursorBlink: false,
        disableStdin: true,
        theme: {
          background: '#f5f5f5', // 浅灰色背景
          foreground: "black",  // 黑色文字
          selectionBackground: '#cccccc',   // 浅灰色选中背景色
        //   selectionForeground: '#000000',
        },
        screenKeys: true,
        fontSize: 14,
        fontWeight: 'bold',
        convertEol: true,        // 确保换行符正确处理
        allowTransparency: true,  // 允许透明度
        lineHeight: 1.2,      // 调整行高
        padding: 8
      })
      
      this.fitAddon = new FitAddon()
      this.terminal.loadAddon(this.fitAddon)
      
      this.terminal.open(this.$refs.terminal)
      this.fitAddon.fit()

      if (this.logs) {
        this.terminal.writeln(this.logs)
        setTimeout(() => {
          this.terminal.scrollToTop();
        }, 0)
      }
      this.terminal.attachCustomKeyEventHandler((arg) => {
        if (arg.ctrlKey && arg.code === "KeyC" && arg.type === "keydown") {
          const selection = this.terminal.getSelection();
          if (selection) {
            this.copyToClipboard(selection);
            return false;
          }
        }
        return true;
      });
    },

    onResize() {
      this.fitAddon?.fit()
    }
  }
}
</script>

<style scoped>
.terminal-container {
  width: 100%;
  overflow: hidden;
}
</style>