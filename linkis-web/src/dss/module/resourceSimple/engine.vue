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
  <div class="engine-box">
    <Spin
      v-if="loading"
      size="large"
      fix/>
    <div
      class="engine-content"
      v-if="ideEngineList.length > 0">
      <div class="engine-header-bar">
        <h3 class="data-type-title">{{ $t('message.common.resourceSimple.YS') }}</h3>
        <div class="classify">
          <span style="width: auto; white-space: nowrap;">{{ $t('message.common.resourceSimple.FL') }}</span>
          <Select v-model="ideSelectData">
            <Option
              v-for="item in typeList"
              :value="item.value"
              :key="item.value">{{ item.label }}</Option>
          </Select>
        </div>
      </div>
      <div
        class="engine-list"
        v-for="item in ideClassList"
        :key="item">
        <span class="engline-name">{{ calssifyName(item) }}</span>
        <ul class="engine-ul">
          <template
            v-for="(subitem, index) in ideEngineList">
            <Tooltip :key="index" :content="calssifyName(subitem.engineType)" placement="left" :disabled="calssifyName(subitem.engineType).length < 8">
              <li
                class="engine-li"
                :class="[{'active': subitem.isActive}, supportColor(subitem.engineStatus)]"
                v-if="subitem.engineType === item || subitem.engineStatus === item || (item === 'Idle' && (subitem.engineStatus === 'Error' || subitem.engineStatus === 'ShuttingDown' || subitem.engineStatus === 'Dead'))"
                :key="index"
                @click="subitem.isActive = !subitem.isActive">
                <span class="ellipsis">{{ calssifyName(subitem.engineType) }}</span>
                <!-- <SvgIcon class='engine-icon job-content-icon' :class="supportIcon(subitem).className" icon-class="common" style='font-size: 30px;' :color="supportIcon(subitem).color === 'yellow' ? '#f4cf2a': supportIcon(subitem).color"/> -->
                <Icon
                  v-show="subitem.isActive"
                  class="engine-right"
                  :class="supportColor(subitem.engineStatus)"
                  type="md-checkmark"></Icon>
              </li>
            </Tooltip>
          </template>
        </ul>
      </div>
    </div>
    <div
      class="engine-content"
      v-if="boardEngineList.length > 0">
      <div class="engine-header-bar">
        <h3 class="data-type-title">{{ $t('message.common.resourceSimple.ZH') }}</h3>
        <div class="classify">
          <span style="width: auto; white-space: nowrap;">{{ $t('message.common.resourceSimple.FL') }}</span>
          <Select v-model="boardSelectData">
            <Option
              v-for="item in typeList"
              :value="item.value"
              :key="item.value">{{ item.label }}</Option>
          </Select>
        </div>
      </div>
      <div
        class="engine-list"
        v-for="item in boardClassList"
        :key="item">
        <span class="engline-name">{{ calssifyName(item) }}</span>
        <ul class="engine-ul">
          <template
            v-for="(subitem, index) in boardEngineList">
            <Tooltip :key="index" :content="calssifyName(subitem.engineType)" placement="left" :disabled="calssifyName(subitem.engineType).length < 8">
              <li
                class="engine-li"
                :class="[{'active': subitem.isActive}, supportColor(subitem.engineStatus)]"
                v-if="subitem.engineType === item || subitem.engineStatus === item || (item === 'Idle' && (subitem.engineStatus === 'Error' || subitem.engineStatus === 'ShuttingDown' || subitem.engineStatus === 'Dead'))"
                :key="index"
                @click="subitem.isActive = !subitem.isActive">
                <span class="ellipsis">{{ calssifyName(subitem.engineType) }}</span>
                <!-- <SvgIcon class='engine-icon job-content-icon' :class="supportIcon(subitem).className" icon-class="common" style='font-size: 30px;' :color="supportIcon(subitem).color === 'yellow' ? '#f4cf2a': supportIcon(subitem).color"/>-->
                <Icon
                  v-show="subitem.isActive"
                  class="engine-right"
                  :class="supportColor(subitem.engineStatus)"
                  type="md-checkmark"></Icon>
              </li>
            </Tooltip>
          </template>
        </ul>
      </div>
    </div>
    <div
      class="engine-content"
      v-if="otherEngineList.length > 0">
      <div class="engine-header-bar">
        <h3 class="data-type-title">Other</h3>
        <div class="classify">
          <span style="width: auto; white-space: nowrap;">{{ $t('message.common.resourceSimple.FL') }}</span>
          <Select v-model="otherSelectData">
            <Option
              v-for="item in typeList"
              :value="item.value"
              :key="item.value">{{ item.label }}</Option>
          </Select>
        </div>
      </div>
      <div
        class="engine-list"
        v-for="item in otherClassList"
        :key="item">
        <span class="engline-name">{{ calssifyName(item) }}</span>
        <ul class="engine-ul">
          <template
            v-for="(subitem, index) in otherEngineList">
            <Tooltip :key="index" :content="calssifyName(subitem.engineType)" placement="left" :disabled="calssifyName(subitem.engineType).length < 8">
              <li
                class="engine-li"
                :class="[{'active': subitem.isActive}, supportColor(subitem.engineStatus)]"
                v-if="subitem.engineType === item || subitem.engineStatus === item || (item === 'Idle' && (subitem.engineStatus === 'Error' || subitem.engineStatus === 'ShuttingDown' || subitem.engineStatus === 'Dead'))"
                :key="index"
                @click="subitem.isActive = !subitem.isActive">
                <span class="ellipsis">{{ calssifyName(subitem.engineType) }}</span>
                <!--  <SvgIcon class='engine-icon job-content-icon' :class="supportIcon(subitem).className" icon-class="common" style='font-size: 30px;' :color="supportIcon(subitem).color === 'yellow' ? '#f4cf2a': supportIcon(subitem).color"/>-->
                <Icon
                  v-show="subitem.isActive"
                  class="engine-right"
                  :class="supportColor(subitem.engineStatus)"
                  type="md-checkmark"></Icon>
              </li>
            </Tooltip>
          </template>
        </ul>
      </div>
    </div>
    <span
      class="no-data"
      v-if="ideEngineList.length === 0 && boardEngineList.length === 0 && otherEngineList.length === 0">{{ $t('message.common.resourceSimple.ZWSJ') }}</span>
  </div>
</template>
<script>
import api from '@/common/service/api';
export default {
  name: 'Job',
  filters: {

  },
  data() {
    return {
      btnSize: 'small',
      loading: false,
      ideSelectData: 0, // Data Development Classification Selection(数据开发分类选择)
      boardSelectData: 0,
      otherSelectData: 0,
      typeList: [
        {
          value: 0,
          label: this.$t('message.common.resourceSimple.LX'),
        },
        {
          value: 1,
          label: this.$t('message.common.resourceSimple.AZT'),
        },
      ],
      ideEngineList: [],
      boardEngineList: [],
      otherEngineList: [],
      ideClassList: [],
      boardClassList: [],
      otherClassList: [],
    };
  },
  computed: {
    activeList() {
      return this.ideEngineList.concat(this.boardEngineList).concat(this.otherEngineList).filter((item) => item.isActive);
    },
  },
  watch: {
    // Select classification, group data(选择分类，分组数据)
    ideSelectData() {
      this.ideClassList = [];
      this.getClassListAction(this.ideSelectData, this.ideEngineList, this.ideClassList);
    },
    boardSelectData() {
      this.boardClassList = [];
      this.getClassListAction(this.boardSelectData, this.boardEngineList, this.boardClassList);
    },
    otherSelectData() {
      this.otherClassList = [];
      this.getClassListAction(this.otherSelectData, this.otherEngineList, this.otherClassList);
    },
    loading(val) {
      this.$emit('change-loading', val);
    },
    activeList(val) {
      let params = !!val.length > 0;
      this.$emit('disabled', !params);
    },
  },
  methods: {
    calssifyName(params) {
      switch (params) {
        case 'spark':
          return 'Spark';
        case 'hive':
          return 'Hive';
        case 'python':
          return 'Python';
        case 'pipeline':
          return 'PipeLine';
        case 'pipeLine':
          return 'PipeLine';
        case 'shell':
          return 'Shell';
        case 'jdbc':
          return 'Jdbc';
        case 'es':
          return 'Es';
        case 'presto':
          return 'Presto';
        case 'flink':
          return 'Flink';
        case 'sqoop':
          return 'Sqoop';
        case 'datax':
          return 'Datax';
        case 'openlookeng':
          return 'Openlookeng';
        case 'elasticsearch':
          return 'Elasticsearch';
        case 'Unlock':
          return this.$t('message.common.resourceSimple.KX')
        case 'Idle':
          return this.$t('message.common.resourceSimple.KX');
        case 'Error':
          return this.$t('message.common.resourceSimple.KX');
        case 'ShuttingDown':
          return this.$t('message.common.resourceSimple.KX');
        case 'Dead':
          return this.$t('message.common.resourceSimple.KX');
        case 'Busy':
          return this.$t('message.common.resourceSimple.FM');
        case 'Starting':
          return this.$t('message.common.resourceSimple.QD');
        default:
          return params;
      }
    },
    killJob() {
      if (this.loading) return this.$Message.warning(this.$t('message.common.resourceSimple.DDJK'));
      const params = [];
      let flage = false;
      this.activeList.map((item) => {
        if (item.engineStatus === 'Starting') flage = true;
        params.push(
          {
            engineType: "EngineConn",
            engineInstance: item.engineInstance,
          }
        );
      });
      if (flage) return this.$Message.warning(this.$t('message.common.resourceSimple.QDYQWFJS'));
      if (params.length === 0) return this.$Message.warning(this.$t('message.common.resourceSimple.WXZYQ'));
      this.loading = true;
      api.fetch(`/linkisManager/rm/enginekill`, params).then(() => {
        this.loading = false;
        setTimeout(() => {
          this.getEngineData();
        }, 3000);
      }).catch(() => {
        this.loading = false;
      });
    },
    selectAll(isSelectedAll) {
      this.ideEngineList.forEach((item) => {
        if (this.supportColor(item.engineStatus) === 'green') item.isActive = !isSelectedAll;
      });
      this.boardEngineList.forEach((item) => {
        if (this.supportColor(item.engineStatus) === 'green') item.isActive = !isSelectedAll;
      });
      this.otherEngineList.forEach((item) => {
        if (this.supportColor(item.engineStatus) === 'green') item.isActive = !isSelectedAll;
      });
    
    },
    getEngineData() {
      if(this.loading) return;
      this.ideEngineList = [];
      this.boardEngineList = [];
      this.ideClassList = [];
      this.boardClassList = [];
      this.otherEngineList = [];
      this.otherClassList = [];
      this.loading = true;
      api.fetch('/linkisManager/rm/engines').then((res) => {
        this.loading = false;
        res.engines.map((item) => {
          item.isActive = false;
          if (item.engineType === 'pipeline') {
            item.engineType = 'pipeLine';
          }
          if (item.creator === 'IDE') {
            this.ideEngineList.push(item);
          } else if (item.creator === 'Visualis') {
            this.boardEngineList.push(item);
          } else {
            this.otherEngineList.push(item);
          }
        });
        // Change data based on state(根据状态改变数据)
        this.getClassListAction(this.ideSelectData, this.ideEngineList, this.ideClassList);
        this.getClassListAction(this.boardSelectData, this.boardEngineList, this.boardClassList);
        this.getClassListAction(this.otherSelectData, this.otherEngineList, this.otherClassList);
      }).catch(() => {
        this.loading = false;
      });
    },
    getClassListAction(selectData, engineList, classList) {
      if (selectData === 0) {
        engineList.map((item) => {
          if (!classList.includes(item.engineType)) {
            classList.push(item.engineType);
          }
        });
      } else {
        engineList.map((item) => {
          if (!classList.includes('Idle') && (item.engineStatus === 'Error' || item.engineStatus === 'ShuttingDown' || item.engineStatus === 'Dead')) {
            classList.push('Idle');
          }
          if (!classList.includes(item.engineStatus) && (item.engineStatus !== 'Error' && item.engineStatus !== 'ShuttingDown' && item.engineStatus !== 'Dead')) {
            classList.push(item.engineStatus);
          }
        });
      }
    },
    // color filter(颜色过滤)
    supportColor(status) {
      switch (status) {
        case 'Busy':
          return 'yellow';
        case 'Error':
          return 'green';
        case 'ShuttingDown':
          return 'green';
        case 'Dead':
          return 'green';
        case 'Idle':
          return 'green';
        case 'Starting':
          return 'blue';
        case 'Unlock':
          return 'green';
        default:
          return 'blue';
      }
    },
    // Icon filtering(图标过滤)
    supportIcon(item) {
      const supportTypes = [
        { rule: 'hive', logo: 'fi-hive' },
        { rule: 'python', logo: 'fi-python' },
        { rule: 'spark', logo: 'fi-spark' },
        { rule: 'pipeLine', logo: 'fi-storage' },
        { rule: 'pipeline', logo: 'fi-storage' },
      ];
      const color = this.supportColor(item.engineStatus);
      const support = supportTypes.filter((type) => type.rule === item.engineType);
      if (support.length > 0) {
        return {
          className: 'is-leaf',
          icon: support[0].logo,
          color: color
        }
      } else {
        return {
          className: '',
          icon: 'fi-scriptis',
          color: color
        }
      }
    },
  },
};
</script>

<style scoped type="text/css">
  .yellow {
    color: #f4cf2a;
  }

  .green {
    color: #19be6b;
  }

  .blue {
    color: #2d8cf0;
  }
  .ellipsis {
    overflow: hidden;
    text-overflow:ellipsis;
    display: inline-block;
    white-space: nowrap;
  }
</style>

