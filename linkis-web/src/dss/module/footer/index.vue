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
  <div
    ref="footerChannel"
    class="layout-footer"
    @mousedown.prevent.stop="onMouseDown"
    @mouseup.prevent.stop="oMouseUp"
    @click.prevent.stop="toast">
    <resource-simple
      ref="resourceSimple"
      @update-job="updateJob">
    </resource-simple>
    <div
      :title="msg"
      class="footer-channel">
      <Icon
        class="footer-channel-job"
        type="ios-swap"/>
      <span class="footer-channel-job-num">{{ num }}</span>
    </div>
  </div>
</template>
<script>
import resourceSimpleModule from '@/dss/module/resourceSimple';
import api from '@/common/service/api';
export default {
  components: {
    resourceSimple: resourceSimpleModule.component,
  },
  data() {
    return {
      num: 0,
      msg: '',
      moveX: null,
      moveY: null,
      isMouseDrop: false,
      isMouseMove: false,
    };
  },
  created() {
    // Keep other interface requests behind the getBasicInfo interface(让其它接口请求保持在getBasicInfo接口后面请求)
    setTimeout(() => {
      this.getRunningJob();
    }, 500);
  },
  watch: {
    '$route'() {
      this.resetChannelPosition();
    }
  },
  methods: {
    getRunningJob() {
      api.fetch('/jobhistory/listundonetasks', {
        pageSize: 100,
        status: 'Running,Inited,Scheduled',
      }, 'get').then((rst) => {
        // Eliminate tasks whose requestApplicationName is "nodeexecution"(剔除requestApplicationName为 "nodeexecution" 的task)
        let tasks = rst.tasks.filter(item => item.requestApplicationName !== "nodeexecution" && item.requestApplicationName !== "CLI")
        this.num = tasks.length;
      });
    },
    'Footer:updateRunningJob'(num) {
      this.num = num;
    },
    'Footer:getRunningJob'(cb) {
      cb(this.num);
    },
    updateJob(num) {
      const method = 'Footer:updateRunningJob';
      this[method](num);
    },
    toast() {
      // Automatic click event after cancel dragging(取消拖动后自动点击事件)
      if (this.isMouseMove) {
        return;
      }
      this.$refs.resourceSimple.open();
    },
    onMouseDown(e) {
      e = e || window.event;
      const footerChannel = this.$refs.footerChannel;
      this.moveX = e.clientX - footerChannel.offsetLeft;
      this.moveY = e.clientY - footerChannel.offsetTop;
      this.isMouseDrop = true;
      this.isMouseMove = false;
      // Prevent text selection during dragging(阻止拖拽过程中选中文本)
      document.onselectstart = () => {
        return false;
      }
      // You can't use @mousemove on elements here, otherwise dragging will freeze(这里无法在元素上使用@mousemove，否则拖动会有卡顿)
      // Using setTimeout is to prevent the click from triggering the move event at the same time. At this time, the normal click event will not be triggered.(使用setTimeout是防止点击的同时会触发move事件，这时正常的点击事件是不会触发的。)
      setTimeout(() => {
        document.onmousemove = (e) => {

          if (this.isMouseDrop) {
            this.isMouseMove = true;
            const footerChannel = this.$refs.footerChannel;
            let x = e.clientX - this.moveX;
            let y = e.clientY - this.moveY;
            // Limit drag range(限制拖动范围)
            let maxX = document.documentElement.clientWidth - 120;
            let maxY = document.documentElement.clientHeight - 60;
            if (this.moveX <= 0) {
              maxX = document.documentElement.scrollWidth - 120;
            }
            if (this.moveY <= 0) {
              maxY = document.documentElement.scrollHeight - 60;
            }
            x = Math.min(maxX, Math.max(0, x));
            y = Math.min(maxY, Math.max(0, y));
            if(e.clientX > maxX+120 || e.clientY > maxY+60 || e.clientX < 0 || e.clientY < 0){
              this.oMouseUp()
            }
            footerChannel.style.left = x + 'px';
            footerChannel.style.top = y + 'px';
          }
        }
      }, 0)
    },
    oMouseUp() {
      // Empty the onmousemove method(清空onmousemove方法)
      document.onmousemove = null;
      this.isMouseDrop = false;
      setTimeout(() => {
        this.isMouseMove = false;
      }, 200)
      // Restore the text selection function of the document(恢复document的文本选中功能)
      document.onselectstart = () => {
        return true;
      }
    },
    resetChannelPosition() {
      const footerChannel = this.$refs.footerChannel;
      footerChannel.style.left = document.documentElement.clientWidth - 120 + 'px';
      footerChannel.style.top = document.documentElement.clientHeight - 60 + 'px';
    }
  },
};
</script>
<style src="./index.scss" lang="scss"></style>
