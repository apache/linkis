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
    <div class="editor-area" :style="{ width, height }"></div>
</template>

<script lang="ts">
// import { debounce } from 'lodash';
import { defineComponent } from 'vue';
// import monaco from './monaco-loader';
import { useMonaco } from './util';

// const types = {
//     code: {
//         theme: 'defaultView',
//     },
//     log: {
//         language: 'log',
//         theme: 'logview',
//         readOnly: true,
//         glyphMargin: false,
//         selectOnLineNumbers: false,
//         wordWrap: 'on',
//     },
// };

export default defineComponent({
    props: {
        width: {
            type: String,
            default: '100%',
        },
        height: {
            type: String,
            default: '90vh',
        },
        language: {
            type: String,
            default: 'json',
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
        // modelValue(val) {
        //     val !== this.getEditor()?.getValue() && this.updateMonacoVal(val);
        // },
        // editorName() {
        //     return `we-editor-${this.type}`;
        // },
        // currentConfig() {
        //     const typeConfig = types[this.type];
        //     const config = merge(
        //         {
        //             automaticLayout: false,
        //             scrollBeyondLastLine: false,
        //             minimap: {
        //                 enabled: false,
        //             },
        //             readOnly: this.readOnly,
        //             glyphMargin: true,
        //         },
        //         typeConfig,
        //         this.options,
        //         {
        //             value: this.value,
        //             theme: this.theme,
        //             language: this.language,
        //         },
        //     );
        //     return config;
        // },
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
            editorModel: null,
            decorations: null,
            isParserClose: true, // Syntax validation is turned off by default(默认关闭语法验证)
            closeParser: null,
            openParser: null,
            sqlParser: null,
            monaco: null,
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
        // initialization(初始化)
        // initMonaco() {
        //     this.editor = monaco.editor.create(this.$el, this.currentConfig) as any;
        //     this.monaco = monaco as any;
        //     this.editorModel = this.editor.getModel();
        //     if (this.type !== 'log') {
        //         if (this.scriptType !== 'hdfsScript' && !this.readOnly) {
        //             this.addCommands();
        //             this.addActions();
        //         }
        //         if (this.language === 'hql') {
        //             this.initParser();
        //         } else {
        //             this.deltaDecorations(this.value);
        //         }
        //     }
        //     this.$emit('onload');
        //     this.editor.onDidChangeModelContent(
        //         debounce(() => {
        //             this.$emit('input', this.getValue());
        //         }),
        //         100,
        //     );
        //     this.editor.onContextMenu(
        //         debounce(() => {
        //             // The right-click menu function that needs to change the text(需要调换文字的右键菜单功能)
        //             const selectList = [
        //                 {
        //                     label: 'Change All Occurrences',
        //                     text: '改变所有出现',
        //                 },
        //                 { label: 'Format Document', text: '格式化' },
        //                 { label: 'Command Palette', text: '命令面板' },
        //                 { label: 'Cut', text: '剪切' },
        //                 { label: 'Copy', text: '复制' },
        //             ];
        //             if (localStorage.getItem('locale') === 'zh-CN') {
        //                 selectList.forEach((item) => {
        //                     const elmentList = document.querySelectorAll(
        //                         `.actions-container .action-label[aria-label="${item.label}"]`,
        //                     );
        //                     this.changeInnerText(elmentList, item.text);
        //                 });
        //             }
        //         }),
        //         100,
        //     );
        // },
    },
    mounted() {
        if (this.$el) {
            // this.editor = monaco.editor.create(this.$el, this.currentConfig);
            // this.monaco = monaco;
            // this.editorModel = this.editor.getModel();
            this.editor = this.createEditor(
                this.$el,
                this.$props.editorOptions,
            ) as any;
            this.updateMonacoVal();
            // this.editor!.onDidChangeModelContent(() => {
            //     this.$emit('update:modelValue', monacoEditor!.getValue());
            // });
            // this.editor!.onDidBlurEditorText(() => {
            //     this.$emit('blur');
            // });
        }
    },
    beforeUnmount() {
        // 销毁 editor，进行gc(销毁 editor，进行gc)
        (this.editor as any)?.dispose();
    },
});
</script>

<style lang="less" scoped>
.editor-area {
    position: relative;
    border: 1px solid #ddd;
    overflow: hidden;
    background-color: #fff;
    box-sizing: border-box;

    .tools {
        z-index: 888;
        position: absolute;
        display: flex;
        flex-direction: column;
        height: 100%;
        padding: 0 2px;
        border-right: 1px solid rgba(0, 0, 0, 0.1);
        left: 0;
        bottom: 0px;
        top: 0;

        .expand {
            cursor: pointer;
            line-height: 0;
            margin-top: 5px;
        }
    }
}
</style>
