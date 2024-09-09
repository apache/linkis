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
    <FDrawer
        v-model:show="show"
        placement="bottom"
        @ok="show = false"
        :closable="false"
        :height="drawerHeight"
        contentClass="history-management-drawer-content"
    >
        <template #title>
            <div class="drawer-title">
                <div class="buttons">
                    <div class="button" @click="expandDrawer" v-if="!isFullScreen">
                        {{ $t('message.common.fullScreen') }}
                    </div>
                    <div class="button" @click="foldDrawer" v-else>
                        {{ $t('message.linkis.fold') }}
                    </div>
                    <div class="delimiter"></div>
                    <div class="button" @click="show = false">
                        {{ $t('message.linkis.close') }}
                    </div>
                </div>
                <div class="title-text">
                    {{ $t('message.linkis.tableColumns.engineInstance') }}：{{ engine?.serviceInstance }}
                </div>
                <FTabs @change="onDrawerTabChange" class="drawer-tabs" v-model="activeDrawerTabPane">
                    <template v-for="tab in tabs" :key="tab.name">
                        <FTabPane :name="t(tab.name)" :value="tab.value" />
                    </template>
                </FTabs>
            </div>
        </template>
        <template #default>
            <Log v-if="activeDrawerTabPane === 1" :engine="props.engine" :isFullScreen="isFullScreen" />
        </template>
    </FDrawer>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import Log from './log.vue';

const { t } = useI18n();

const props = defineProps<{
    engine: any,
}>();

enum TAB_PANE_VALUE {
    'ENGINE_LOG' = 1,
}

const tabs = [
    { name: 'message.common.log', value: TAB_PANE_VALUE.ENGINE_LOG },
];

const activeDrawerTabPane = ref(TAB_PANE_VALUE.ENGINE_LOG);
const show = ref(false);
const drawerHeight = ref(520);
const isFullScreen = ref(false);

const open = () => {
    show.value = true;
};

const expandDrawer = () => {
    drawerHeight.value = document.body.clientHeight;
    isFullScreen.value = true;
};
const foldDrawer = () => {
    drawerHeight.value = 520;
    isFullScreen.value = false;
};

const onDrawerTabChange = (value: TAB_PANE_VALUE) => {
    activeDrawerTabPane.value = value;
};

defineExpose({
    open,
});
</script>

<style lang="less">
.history-management-drawer-content {
    .fes-drawer-header {
        padding: 0;
        .fes-tabs-tab-pane-wrapper {
            display: none;
        }
    }
    .fes-scrollbar-container {
        padding: 0;
    }
}
</style>
<style scoped lang="less">
.drawer-title {
    width: 100%;
    position: relative;
    .buttons {
        position: absolute;
        right: 16px;
        top: 5px;
        display: flex;
        gap: 12px;
        font-size: 14px;
        color: #63656f;
        line-height: 22px;
        font-weight: 400;
        .delimiter {
            width: 1px;
            height: 16px;
            background: #e3e8ee;
            transform: translateY(3px);
        }
        .button {
            cursor: pointer;
        }
    }
    .title-text {
        height: 32px;
        padding: 6px 20px;
        font-family: PingFangSC-Medium;
        font-size: 14px;
        color: #0f1222;
        font-weight: bold;
    }
    .drawer-tabs {
        background-color: #f8f9fc;
        padding-left: 4px;
    }
}
</style>
