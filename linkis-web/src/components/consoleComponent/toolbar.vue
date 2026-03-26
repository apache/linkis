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
    class="we-toolbar-wrap">
    <ul
      ref="toolbar"
      class="we-toolbar">
      <li
        v-if="(script.runType === 'sql' || script.runType === 'hql' ||(script.runType === 'py' && script.resultList[script.resultSet].result.type === '2')) && this.$route.name === 'Home' && script.resultList !== null">
        <Poptip
          :transfer="true"
          :width="220"
          v-model="popup.visualis"
          placement="right"
          popper-class="we-poptip">
          <div @click.stop="showResultAction">
            <Icon v-if="visualShow=='table'" type="md-analytics" :size="20"/>
            <Icon v-if="visualShow=='visual' || visualShow=='dataWrangler'" type="ios-grid" :size="iconSize"/>
            <span v-if="isIconLabelShow" :title="$t('message.common.toolbar.analysis')" class="v-toolbar-icon">{{ analysistext.text }}</span>
          </div>
          <div slot="content">
            <div>
              <Row>
                {{$t('message.common.toolbar.model')}}
              </Row>
              <Row>
                <RadioGroup v-model="resultsShowType">
                  <Col span="10">
                    <Radio label="1">{{$t('message.common.toolbar.graphAnalysis')}}</Radio>
                  </Col>
                  <Col
                    span="10"
                    offset="4">
                    <Radio label="2">{{$t('message.common.toolbar.excelAnalysis')}}</Radio>
                  </Col>
                </RadioGroup>
              </Row>
            </div>
            <Row class="confirm">
              <Col span="10">
                <Button @click="cancelPopup('visualis')">{{$t('message.common.cancel')}}</Button>
              </Col>
              <Col
                span="10"
                offset="4">
                <Button
                  type="primary"
                  @click="confirm('visualis')">{{ $t('message.common.submit') }}</Button>
              </Col>
            </Row>
          </div>
        </Poptip>
      </li>
      <li v-if="analysistext.flag !== 2 && rsDownload && canDownload" :style="{cursor: rsDownload ? 'pointer': 'not-allowed'}">
        <Poptip
          :transfer="true"
          :width="500"
          v-model="popup.download"
          placement="right"
          popper-class="we-poptip">
          <div @click.stop="openPopup('download')">
            <i class="material-icons">cloud_download</i> 
            <span v-if="isIconLabelShow" :title="$t('message.common.download')" class="v-toolbar-icon">{{ $t('message.common.download') }}</span>
          </div>
          <div slot="content">
            <div>
              <Row>
                {{ $t('message.common.toolbar.format') }}
              </Row>
              <Row>
                <RadioGroup v-model="download.format">
                  <Col span="10">
                    <Radio label="1">CSV</Radio>
                  </Col>
                  <Col
                    v-if="resultType === '2'"
                    span="10"
                    offset="4">
                    <Radio label="2">Excel</Radio>
                  </Col>
                </RadioGroup>
              </Row>
            </div>
            <div>
              <Row>
                {{ $t('message.common.toolbar.coding') }}
              </Row>
              <Row>
                <RadioGroup v-model="download.coding">
                  <Col span="10">
                    <Radio label="1">UTF-8</Radio>
                  </Col>
                  <Col
                    span="10"
                    offset="4">
                    <Radio label="2">GBK</Radio>
                  </Col>
                </RadioGroup>
              </Row>
            </div>
            <div>
              <Row>
                {{$t('message.common.toolbar.replace')}}
              </Row>
              <Row>
                <RadioGroup v-model="download.nullValue">
                  <Col span="10">
                    <Radio label="1">NULL</Radio>
                  </Col>
                  <Col
                    span="10"
                    offset="4">
                    <Radio label="2">{{$t('message.common.toolbar.emptyString')}}</Radio>
                  </Col>
                </RadioGroup>
              </Row>
            </div>
            <div v-if="+download.format === 1">
              <Row>
                {{$t('message.common.toolbar.selectSeparator')}}
              </Row>
              <Row>
                <RadioGroup v-model="download.csvSeparator" style="width: 100%;">
                  <Col v-for="item in separators" :key="item.value" :span="item.span" :offset="item.offset">
                    <Radio :label="item.value">{{item.label}}</Radio>
                  </Col>
                </RadioGroup>
              </Row>
            </div>
            
            <Row>
              {{$t('message.common.toolbar.downloadMode')}}
            </Row>
            <div v-if="isAll">
              <Row>
                <Checkbox v-model="allDownload">{{$t('message.common.toolbar.all', {count: String(allPath.length)})}}</Checkbox>
              </Row>
            </div>
            
            <div v-if="isExcel">
              <Row>
                <Checkbox v-model="autoFormat">{{$t('message.common.toolbar.autoFormat')}}</Checkbox>
              </Row>
            </div>
            <Row class="confirm">
              <Col span="10">
                <Button @click="cancelPopup('download')">{{$t('message.common.cancel')}}</Button>
              </Col>
              <Col
                span="10"
                offset="4">
                <Button
                  type="primary"
                  @click="confirm('download')">{{ $t('message.common.submit') }}</Button>
              </Col>
            </Row>
          </div>
        </Poptip>
      </li>
      <li
        @click="openPopup('export')"
        v-if="$route.name === 'Home' && analysistext.flag !== 2">
        <SvgIcon :style="{ 'font-size': '20px' }" icon-class="common" color="#515a6e"/>
        <span
          class="v-toolbar-icon"
          v-if="isIconLabelShow">{{ $t('message.common.toolbar.export') }}</span>
      </li>
      <li
        @click="openPopup('rowView')"
        v-if="row"
        :title="$t('message.common.toolbar.rowToColumnTitle')">
        <SvgIcon :style="{ 'font-size': '20px' }" icon-class="common" color="#515a6e"/>
        <span v-if="isIconLabelShow" class="v-toolbar-icon">{{$t('message.common.toolbar.rowToColumn')}}</span>
      </li>
      <li
        @click="openPopup('filter')"
        :title="$t('message.common.toolbar.resultGroupLineFilter')"
        v-if="showFilter">
        <SvgIcon :style="{ 'font-size': '20px' }" icon-class="common" color="#515a6e"/>
        <span
          class="v-toolbar-icon"
          v-if="isIconLabelShow">{{$t('message.common.toolbar.lineFilter')}}</span>
      </li>
    </ul>
    <results-export
      ref="exportToShare"
      :script="script"
      :dispatch="dispatch"
      :current-path="currentPath">
    </results-export>
    <table-row
      ref="tableRow"
      :row="row">
    </table-row>
  </div>
</template>
<script>
import moment from 'moment';
import elementResizeEvent from '@/common/helper/elementResizeEvent';
import resultsExport from './resultsExport.vue';
import tableRow from './tableRow.vue';
import mixin from '@/common/service/mixin';
import api from '@/common/service/api';
import 'material-design-icons/iconfont/material-icons.css';
import storage from '@/common/helper/storage';
export default {
  components: {
    resultsExport,
    tableRow,
  },
  mixins: [mixin],
  props: {
    visualShow: String,
    currentPath: {
      type: String,
      default: '',
    },
    allPath: {
      type: Array,
      default: () => []
    },
    showFilter: {
      type: Boolean,
      default: false
    },
    script: [Object],
    row: {
      type: Array,
      default: () => [],
    },
    dispatch: {
      type: Function,
      required: true
    },
    getResultUrl: {
      type: String,
      defalut: `filesystem`
    },
    resultType: {
      type: String,
      defalut: ''
    },
    comData: {
      type: Object
    }
  },
  data() {
    return {
      popup: {
        download: false,
        export: false,
        visual: false,
        rowView: false,
        visualis: false
      },
      download: {
        format: '1',
        coding: '1',
        nullValue: '1',
        csvSeparator: '1',
      },
      isIconLabelShow: true,
      iconSize: 14,
      allDownload: false, // whether to download all result sets(是否下载全部结果集)
      autoFormat: false, // whether to format result sets
      resultsShowType: '2',
      separators: [
        { key: ',', label: this.$t('message.common.separator.comma'), value: '1', span: 4, offset: 0},
        { key: '\t', label: this.$t('message.common.separator.tab'), value: '2', span: 4, offset: 1},
        { key: ' ', label: this.$t('message.common.separator.space'), value: '3', span: 4, offset: 1},
        { key: '|', label: this.$t('message.common.separator.vertical'), value: '4', span: 4, offset: 1 },
        { key: ';', label: this.$t('message.common.separator.semicolon'), value: '5', span: 4, offset: 1}
      ]
    };
  },
  computed: {
    analysistext: function(){
      let describe = '';
      this.visualShow === "table"? describe = {
        flag: 1,
        text: this.$t('message.common.toolbar.deepAnalysis')
      } : describe = {
        flag: 2,
        text: this.$t('message.common.toolbar.resultGroup')
      };
      return describe
    },
    canDownload() {
      if (this?.script?.resultList && this.script.resultList[this.script.resultSet]?.result?.tipMsg) {
        return !this.script.resultList[this.script.resultSet].result.tipMsg;
      }
      return true
    },
    isAll() {
      return ['hql', 'sql', 'tsql', 'nebula'].includes(this.script.runType) || this.download.format === '1';
    },
    isExcel() {
      return this.download.format === '2';
    },
    rsDownload() {
      return storage.get('resultSetExportEnable');
    }
  },
  mounted() {
    elementResizeEvent.bind(this.$el, this.resize);
  },
  methods: {
    openPopup(type) {
      if (type === 'download') {
        this.download = {
          format: '1',
          coding: '1',
          nullValue: '1',
          csvSeparator: '1',
        }
      }
      if (type === 'export') {
        this.$refs.exportToShare.open();
      } else if (type === 'rowView') {
        this.$refs.tableRow.open();
      } else if (type === 'filter') {
        this.$emit('on-filter');
      } else {
        this.popup[type] = true;
      }
    },
    cancelPopup(type) {
      this.popup[type] = false;
    },
    confirm(type) {
      this[`${type}Confirm`]();
      this.cancelPopup(type);
    },
    showResultAction() {
      // When toggling the result set visualization button(当切换结果集可视化按钮时)
      if (this.visualShow=='table') {
        this.popup.visualis = true;
      } else {
        this.$emit('on-analysis', 'table');
      }
    },
    visualisConfirm() {
      const resultType = this.resultsShowType === '1' ? 'visual' : 'dataWrangler';
      this.$emit('on-analysis', resultType);
    },
    downloadFromHref(url) {
      const link = document.createElement('a');
      link.setAttribute('href', url);
      link.setAttribute('download', '');
      const evObj = document.createEvent('MouseEvents');
      evObj.initMouseEvent('click', true, true, window, 0, 0, 0, 0, 0, false, false, true, false, 0, null);
      return link.dispatchEvent(evObj)
    },
    pause(msec = 1000) {
      return new Promise((resolve) => {
        setTimeout(resolve, msec);
      })
    },
    async downloadConfirm() {
      const splitor = this.download.format === '1' ? 'csv' : 'xlsx';
      const charset = this.download.coding === '1' ? 'utf-8' : 'gbk';
      const nullValue = this.download.nullValue === '1' ? 'NULL' : 'BLANK';
      const timestamp = moment.unix(moment().unix()).format('MMDDHHmm');
      let fileName = ''
      if (this.$route.query.fileName && this.$route.query.fileName !== 'undefined') {
        fileName = this.$route.query.fileName
      } else if ( this.script.fileName && this.script.fileName !== 'undefined') {
        fileName = this.script.fileName
      } else if(this.script.title && this.script.title !== 'undefined') {
        fileName = this.script.title
      }
      const filename = `Result_${fileName}_${timestamp}`;
      let temPath = this.currentPath;
      // The result set path of the api execution download is different(api执行下载的结果集路径不一样)
      let apiPath = `${this.getResultUrl}/resultsetToExcel`;
      if (this.isAll && this.allDownload && this.isExcel) {
        temPath = temPath.substring(0, temPath.lastIndexOf('/'));
        apiPath = `${this.getResultUrl}/resultsetsToExcel`
      }
      let url = `http://${window.location.host}/api/rest_j/v1/` + apiPath + '?charset=' + charset + '&outputFileType=' + splitor + '&nullValue=' + nullValue;
      // If the api execution page gets the result set, you need to bring the taskId(如果是api执行页获取结果集，需要带上taskId)
      if(this.getResultUrl !== 'filesystem') {
        url += `&taskId=${this.comData.taskID}`
      }
      if (+this.download.format === 1) {
        let separatorItem = this.separators.find(item => item.value === this.download.csvSeparator) || {};
        let separator = encodeURIComponent(separatorItem.key || '');
        url += `&csvSeparator=${separator}`
      }
      if(this.isExcel) {
        url += `&autoFormat=${this.autoFormat}`
      }
      // Before downloading, use the heartbeat interface to confirm whether to log in(下载之前条用心跳接口确认是否登录)
      await api.fetch('/user/heartbeat', 'get');
      const eventList = [];
      let flag = null;
      if (this.isAll && !this.isExcel && this.allDownload) {
        let count = 0
        for(let [index, path] of this.allPath.sort((a, b) => {
          return +(a.split('.')[0].substring(1)) - +(b.split('.')[0].substring(1))
        }).entries()) {
          let temUrl = url;
          temUrl += `&path=${path}`
          const name = `ResultSet${index + 1}`
          temUrl += '&outputFileName=' + name
          const event = this.downloadFromHref(temUrl)
          eventList.push(event);
          if(++count >= 10) {
            await this.pause(1000);
            count = 0;
          }
        }
        // this.allPath.forEach(path => {
        //   let temUrl = url;
        //   temUrl += `&path=${path}`
        //   const name = `ResultSet${Number(path.substring(temPath.lastIndexOf('/')).split('.')[0].split('_')[1]) + 1}`
        //   temUrl += '&outputFileName=' + name
        //   const event = this.downloadFromHref(temUrl)
        //   eventList.push(event);
        // });
      } else {
        url += `&path=${temPath}` + '&outputFileName=' + filename
        flag = this.downloadFromHref(url);
      }
      this.$nextTick(() => {
        if (flag && this.isExcel) {
          this.$Message.success(this.$t('message.common.toolbar.success.download'));
        } else if (!this.isExcel && !eventList.includes(false)) {
          this.$Message.success(this.$t('message.common.toolbar.success.download'));
        }
      });
    },
    // analysis() {
    //   this.$emit('on-analysis');
    // },
    resize() {
      const height = window.getComputedStyle(this.$refs.toolbar).height;
      if (parseInt(height) <= 190) {
        this.isIconLabelShow = false;
        this.iconSize = 20;
      } else {
        this.isIconLabelShow = true;
        this.iconSize = 14;
      }
    }
  },
  beforeDestroy() {
    elementResizeEvent.unbind(this.$el, this.resize);
  },
};
</script>
<style lang="scss" scoped>
@import '@/common/style/variables.scss';
  .we-toolbar-wrap {
    width: $toolbarWidth;
    height: 100%;
    word-break: break-all;
    position: $absolute;
    left: 40px;
    margin-left: -$toolbarWidth;
    background: $body-background;
    .we-poptip {
      padding: 12px;
      line-height: 28px;
      .confirm {
        margin-top: 10px;
      }
      .title {
        margin: 10px 0;
        text-align: center;
      }
    }
    .we-toolbar {
      width: 100%;
      height: 100%;
      border-right: 1px solid #dcdee2;
      padding-top: 10px;
      li {
        padding-bottom: 20px;
        text-align: center;
        cursor: pointer;
      }
      span {
        display: block;
        line-height: 24px
      }
    }
  }
</style>

