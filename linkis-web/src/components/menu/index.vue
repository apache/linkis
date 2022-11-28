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
    v-clickoutside="handleOutsideClick"
    v-show="isShow"
    :style="ctxStyle"
    :id="`menu-${id}`"
    class="ctx-menu-container"
    @click.stop="handleSelect"
    @contextmenu.stop>
    <div class="ctx">
      <ul
        class="ctx-menu">
        <slot/>
      </ul>
    </div>
  </div>
</template>
<script>
import clickoutside from '@/common/helper/clickoutside';

export default {
  name: 'WeMenu',
  directives: {
    clickoutside,
  },
  props: {
    id: String,
  },
  data() {
    return {
      isShow: false,
      ctxTop: 0,
      ctxLeft: 0,
      ctxBottom: 0,
      ctxRight: 0,
    };
  },
  computed: {
    ctxStyle() {
      const style = {
        top: `${this.ctxTop}px`,
        right: `${this.ctxRight}px`,
        bottom: `${this.ctxBottom}px`,
        left: `${this.ctxLeft}px`,
      };
      if (this.ctxTop === -1) {
        delete style.top;
      }
      if (this.ctxRight === -1) {
        delete style.right;
      }
      if (this.ctxBottom === -1) {
        delete style.bottom;
      }
      if (this.ctxLeft === -1) {
        delete style.left;
      }
      return style;
    },
  },
  methods: {
    setPositionFromEvent(e) {
      const { pageX, pageY } = e;
      // height of the window(窗口的高度)
      const innerHeight = window.innerHeight;
      const innerwidth = window.innerWidth;
      const top = pageY - (document.body.scrollTop);
      this.ctxTop = top;
      this.ctxLeft = pageX;
      this.ctxBottom = -1;
      this.ctxRight = -1;
      this.$nextTick(() => {
        const menu = document.getElementById(`menu-${this.id}`);
        const menuHeight = menu.scrollHeight;
        const menuWidth = menu.scrollWidth;
        if (top + menuHeight >= innerHeight) {
          this.ctxBottom = innerHeight - pageY + menuHeight;
          this.ctxTop = -1;
        }
        if (pageX + menuWidth >= innerwidth) {
          this.ctxRight = innerwidth - pageX + menuWidth;
          this.ctxLeft = -1;
        }
      });
    },
    handleOutsideClick() {
      this.close();
      this.$emit('we-menu-cancel');
    },
    handleSelect() {
      this.close();
      this.$emit('we-menu-select');
    },
    show() {
      this.isShow = true;
    },
    close() {
      this.isShow = false;
    },
    open(e) {
      this.show();
      this.setPositionFromEvent(e);
    },
  },
};
</script>
<style lang="scss" src="./index.scss"></style>
