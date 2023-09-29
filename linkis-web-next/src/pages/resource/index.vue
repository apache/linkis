<template>
    <FTabs @change="onTabChange">
        <FTabPane name="资源管理" value="/console/resource/manage"></FTabPane>
        <FTabPane
            name="历史管理"
            value="/console/resource/history"
            displayDirective="show"
        >
        </FTabPane>
    </FTabs>
    <router-view></router-view>
</template>
<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
const onTabChange = (key: string) => {
    router.push(key);
};
</script>
<style lang="less" scoped>
:deep(.fes-tabs-tab-pane-wrapper) {
    display: none;
}
</style>
