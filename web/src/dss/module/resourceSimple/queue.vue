<template>
  <div class="queue-manager">
    <Spin
      v-if="loading"
      size="large"
      fix/>
    <div
      class="queue-manager-select-warpper"
      v-if="queueList.length">
      <Select
        v-model="current"
        class="queue-manager-select"
        :placeholder="$t('message.common.resourceSimple.XZDL')"
        @on-change="getQueueInfo">
        <Option
          v-for="(queue, index) in queueList"
          :key="index"
          :value="queue.text">{{ queue.text }}</Option>
      </Select>
    </div>
    <div v-if="infos">
      <div class="queue-manager-used">
        <div class="queue-manager-title">{{ $t('message.common.resourceSimple.ZYSYL') }}</div>
        <div class="queue-manager-circle-warpper">
          <v-circle
            :percent="infos.queueInfo.usedPercentage.cores"
            :used="infos.queueInfo.usedResources.cores.toString()"
            :max="infos.queueInfo.maxResources.cores.toString()"
            :suffixe="$t('message.common.resourceSimple.H')"
            width="120px"
            height="120px"
            title="CPU"
            class="queue-manager-circle"></v-circle>
          <v-circle
            :percent="infos.queueInfo.usedPercentage.memory"
            :used="formatToGb(infos.queueInfo.usedResources.memory)"
            :max="formatToGb(infos.queueInfo.maxResources.memory)"
            suffixe="GB"
            width="120px"
            height="120px"
            :title="$t('message.common.resourceSimple.NC')"></v-circle>
        </div>
      </div>
      <div class="queue-manager-top">
        <span class="queue-manager-title">{{ $t('message.common.resourceSimple.ZYSYPHB') }}</span>
        <div
          class="queue-manager-top-content"
          v-if="infos.userResources.length">
          <div
            v-for="(item, index) in infos.userResources"
            :key="index"
            class="queue-manager-item">
            <span class="queue-manager-name">{{ item.username }}</span>
            <Tooltip
              :content="`${formatToPercent(item.idlePercentage)}${$t('message.common.resourceSimple.KX')} / ${formatToPercent(item.busyPercentage)}${$t('message.common.resourceSimple.FM')}`"
              placement="top">
              <span class="queue-manager-status">
                <span
                  class="queue-manager-status-busy"
                  :style="{'width': formatToPercent(item.busyPercentage)}"
                  :title="formatToPercent(item.busyPercentage)">
                  <i class="queue-manager-status-label"></i>
                </span><span
                  class="queue-manager-status-idle"
                  :style="{'width': formatToPercent(item.busyPercentage + item.idlePercentage)}"
                  :title="formatToPercent(item.idlePercentage)">
                  <i class="queue-manager-status-label"></i>
                </span>
              </span>
            </Tooltip>
            <span class="queue-manager-total">{{ formatToPercent(item.totalPercentage) }}</span>
          </div>
        </div>
        <div
          class="queue-manager-item"
          v-else>{{ $t('message.common.resourceSimple.ZW') }}</div>
      </div>
    </div>
  </div>
</template>
<script>
import api from '@/common/service/api';
import circle from '@/components/circleProgress/index.vue';
export default {
  components: {
    'v-circle': circle,
  },
  data() {
    return {
      queueList: [],
      current: '',
      infos: null,
      loading: true,
    };
  },
  methods: {
    getQueueList() {
      this.loading = true;
      // 获取队列资源使用状态
      api.fetch('/linkisManager/rm/queues').then((res) => {
        this.loading = false;
        this.queueList = [];
        // 将显示字段进行拼接显示并分别存储对应参数
        res.queues.forEach(item => {
          item.queues.forEach(i => {
            let obj ={};
            obj.clustername = item.clustername;
            obj.queuename = i;
            obj.text = `${item.clustername}-${i}`
            this.queueList.push(obj)
          })
        });
        this.current = this.queueList[0] ? this.queueList[0].text : '';
        this.getQueueInfo(this.queueList[0].text);
      }).catch(() => {
        this.loading = false;
      });
    },
    getQueueInfo(name = '') {
      // 根据返回的显示数据找出对应的完整对象
      name = this.queueList.find(item => item.text === name) || {};
      this.loading = true;
      // 取出对应的参数
      let clustername = name.clustername;
      let queuename = name.queuename;
      // 获取队列资源数据
      api.fetch('/linkisManager/rm/queueresources', {
        queuename,
        clustername
      }).then((res) => {
        this.loading = false;
        this.infos = res;
      }).catch(() => {
        this.loading = false;
      });
    },
    formatToPercent(val) {
      return (val * 100).toFixed(0) + '%';
    },
    formatToGb(val) {
      return (val / 1024 / 1024 / 1024).toFixed(0);
    },
  },
};
</script>
