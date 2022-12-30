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
  <Poptip
    popper-class="fn-view"
    transfer
    trigger="hover"
    width="540"
    placement="right">
    <slot></slot>
    <div slot="content">
      <canvas
        :id="`cvs${node.id}`"
        class="fn-view-canvas"/>
      <Form
        v-if="node"
        :model="node"
        :label-width="100"
        size="small">
        <FormItem :label="$t('message.common.functionView.formItems.name') + '：'">
          <span
            :title="node.name"
            class="fn-item">{{ node.name }}</span>
        </FormItem>
        <FormItem :label="$t('message.common.functionView.formItems.createUser') + '：'">
          <span>{{ node.createUser }}</span>
        </FormItem>
        <FormItem :label="$t('message.common.functionView.formItems.updateTime') + '：'">
          <span>{{ dateFormatter(node.updateTime) }}</span>
        </FormItem>
        <!--<FormItem label="注册格式：">
                    <span>{{node.registerFormat}}</span>
                </FormItem>-->
        <FormItem :label="$t('message.common.functionView.formItems.useFormat') + '：'">
          <span
            :title="node.useFormat"
            class="fn-item">{{ node.useFormat }}</span>
        </FormItem>
        <FormItem :label="$t('message.common.functionView.formItems.description') + '：'">
          <span
            :title="node.description"
            class="fn-item">{{ node.description }}</span>
        </FormItem>
      </Form>
    </div>
  </Poptip>
</template>
<script>
import moment from 'moment';
export default {
  props: {
    node: {
      type: Object,
      default: () => {},
    },
  },
  data() {
    return {
    };
  },
  mounted() {
    this.setXY();
  },
  methods: {
    setXY() {
      let type = '';
      let x = null;
      let y = null;
      if (this.node.udfType === 0) {
        type = '通用';
        x = 86;
        y = 106;
      } else if (this.node.udfType > 2) {
        type = '自定义函数';
        x = 60;
        y = 106;
      } else {
        type = 'spark';
        x = 80;
        y = 106;
      }
      this.drawWaterMark(type, x, y);
    },
    drawWaterMark(type, x, y) {
      // Since the id is unique, the id of the canvas here uses a dynamic string, so that the id of each element is different, otherwise it will fail to render.(由于id是唯一的，所以这里canvas的id使用动态的字符串，让每个元素的id都不一样，否则会出现无法渲染的情况)
      const cvs = document.getElementById(`cvs${this.node.id}`);
      const ctx = cvs.getContext('2d');
      cvs.height = 140;
      cvs.width = 140;
      const drawDashRound = (x, y, radius, step = 5) => {
        step = (5 / 180) * Math.PI * 2;
        for (let b = 0, e = step / 2; e <= 360; b += step, e += step) {
          ctx.beginPath();
          ctx.arc(x, y, radius, b, e);
          ctx.stroke();
          ctx.strokeStyle = '#99d7f7';
        }
      };
      drawDashRound(100, 100, 70);
      drawDashRound(100, 100, 50);
      ctx.font = '16px microsoft yahei';
      ctx.fillStyle = '#99d7f7';
      ctx.fillText(type, x, y);
    },
    dateFormatter(date) {
      return moment(date).format('YYYY-MM-DD HH:mm:ss');
    },
  },
};
</script>
<style lang="scss" src="./index.scss">
</style>
