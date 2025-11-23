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
    <div :style="{ width, height }"></div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useMonaco } from './util';

export default defineComponent({
    props: {
        width: {
            type: String,
            default: '99vw',
        },
        height: {
            type: String,
            default: '85vh',
        },
        language: {
            type: String,
            default: 'log',
        },
        preComment: {
            type: String,
            default: '',
        },
        modelValue: {
            type: String,
            default: '',
        },
        editorOptions: {
            type: Object,
            default: () => ({}),
        },
    },
    watch: {
        modelValue(val) {
            val !== this.getEditor()?.getValue() && this.updateMonacoVal(val);
        },
    },
    setup(props) {
        const { updateVal, getEditor, createEditor, onFormatDoc } = useMonaco(
            props.language,
        );
        return {
            updateVal,
            getEditor,
            createEditor,
            onFormatDoc,
            editor: null,
        };
    },
    methods: {
        updateMonacoVal(_val?: string) {
            const { modelValue, preComment } = this.$props;
            const val = preComment
                ? `${preComment}\n${_val || modelValue}`
                : _val || modelValue;
            this.updateVal(val);
        },
    },
    mounted() {
        if (this.$el) {
            this.editor = this.createEditor(
                this.$el,
                this.$props.editorOptions,
            ) as any;
            this.updateMonacoVal();
        }
    },
    beforeUnmount() {
        // 销毁 editor，进行gc
        (this.editor as any)?.dispose();
    },
});
</script>

<style lang="less" scoped>
</style>
