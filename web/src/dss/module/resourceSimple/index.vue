<template>
  <Modal
    v-model="show"
    :transition-names="['none','none']"
    :width="580">
    <div slot="header">
      <div class="header-tab">
        <ButtonGroup
          shape="circle"
          size="large">
          <Button
            :type="switcher === 'job' ? 'primary': 'default'"
            @click="getRunningJobs">{{ $t('message.common.resourceSimple.RWGLQ') }}</Button>
          <Button
            :type="switcher === 'session' ? 'primary': 'default'"
            @click="getEngines">{{ $t('message.common.resourceSimple.YQGLQ') }}</Button>
          <Button
            :type="switcher === 'queue' ? 'primary': 'default'"
            @click="getQueue">{{ $t('message.common.resourceSimple.DLGLQ') }}</Button>
        </ButtonGroup>
      </div>
    </div>
    <div class="resource-simple-content">
      <job
        ref="job"
        v-if="switcher === 'job'"
        @close-modal="close"
        @change-loading="changeLoading"
        @change-job-disabled="changeJobDisabled"/>
      <engine
        ref="engine"
        v-else-if="switcher === 'session'"
        @disabled="engineDisabledChange"
        @change-loading="changeLoading"/>
      <queue
        ref="queue"
        v-else></queue>
    </div>
    <template slot="footer">
      <div
        class="resource-simple-footer legendShow">
        <point
          v-if="pointList.length"
          :point-list="pointList"></point>
        <div>
          <Button
            type="default"
            @click="rest">
            <Icon
              size="18"
              type="ios-repeat"></Icon>
            {{ $t('message.common.resourceSimple.SX') }}
          </Button>
          <Button
            type="default"
            v-if="switcher === 'job'"
            :disabled="isJobBtnDisabled"
            @click="openKillModal">
            {{ $t('message.common.resourceSimple.JSRW') }}
          </Button>
          <Button
            :disabled="engineDisable"
            type="default"
            v-if="switcher === 'session'"
            @click="openKillModal">
            {{ $t('message.common.resourceSimple.JSYQ') }}
          </Button>
        </div>
      </div>
    </template>
    <detele-modal
      ref="killModal"
      :loading="loading"
      @delete="killJob"></detele-modal>
  </Modal>
</template>
<script>
import deteleModal from '@/components/deleteDialog';
import job from './job.vue';
import engine from './engine.vue';
import queue from './queue.vue';
import point from './point.vue';

export default {
  components: {
    job,
    engine,
    queue,
    point,
    deteleModal,
  },
  data() {
    return {
      dataList: [],
      show: false,
      switcher: 'job',
      pointList: [],
      loading: false,
      isJobBtnDisabled: true,
      engineDisable: true,
    };
  },
  methods: {
    getRunningJobs() {
      this.switcher = 'job';
      this.getPointList();
      this.$nextTick(() => {
        this.$refs.job.getJobList();
      });
    },
    getEngines() {
      this.switcher = 'session';
      this.getPointList();
      this.$nextTick(() => {
        this.$refs.engine.getEngineData();
      });
    },
    getQueue() {
      this.switcher = 'queue';
      this.getPointList();
      this.$nextTick(() => {
        this.$refs.queue.getQueueList();
      });
    },
    open() {
      this.show = true;
      if (this.switcher === 'job') {
        this.isJobBtnDisabled = true;
        this.getRunningJobs();
      } else if (this.switcher === 'session') {
        this.getEngines();
      } else {
        this.getQueue();
      }
    },
    close() {
      this.show = false;
    },
    killSuccess() {
      this.getRunningJobs();
    },
    rest() {
      this.open();
    },
    getPointList() {
      let list = null;
      if (this.switcher === 'queue') {
        list = [{
          type: 'idle',
          label: this.$t('message.common.resourceSimple.KX'),
        }, {
          type: 'busy',
          label: this.$t('message.common.resourceSimple.FM'),
        }];
      } else if (this.switcher === 'job') {
        list = [{
          type: 'init',
          label: this.$t('message.common.resourceSimple.PDZ'),
        }, {
          type: 'schedule',
          label: this.$t('message.common.resourceSimple.ZYSQ'),
        }, {
          type: 'running',
          label: this.$t('message.common.resourceSimple.YX'),
        }];
      } else {
        list = [{
          type: 'busy',
          label: this.$t('message.common.resourceSimple.FM'),
        }, {
          type: 'idle',
          label: this.$t('message.common.resourceSimple.KX'),
        }, {
          type: 'starting',
          label: this.$t('message.common.resourceSimple.QD'),
        }];
      }
      this.pointList = list;
    },
    openKillModal() {
      const type = this.switcher === 'session' ? this.$t('message.common.resourceSimple.YQ') : this.$t('message.common.resourceSimple.RW');
      this.$refs.killModal.open({ type, name: '' });
    },
    killJob() {
      if (this.switcher === 'session') {
        this.$refs.engine.killJob();
      } else {
        this.$refs.job.killJob();
      }
    },
    changeLoading(val) {
      this.loading = val;
    },
    changeJobDisabled(val) {
      this.isJobBtnDisabled = val;
    },
    engineDisabledChange(params) {
      this.engineDisable = params;
    },
  },
};
</script>
<style lang="scss" src="./index.scss">
</style>
