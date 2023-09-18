<template>
    <div class="editor-area" :style="{ width, height }"></div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { merge, debounce } from 'lodash';
import storage from '@/common/helper/storage';
import useMonaco from './util2';
import monaco from './monaco-loader';
import highRiskGrammar from './highRiskGrammar';

const types = {
    code: {
        theme: 'defaultView',
    },
    log: {
        language: 'log',
        theme: 'logview',
        readOnly: true,
        glyphMargin: false,
        selectOnLineNumbers: false,
        wordWrap: 'on',
    },
};

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
        modelValue(val) {
            val !== this.getEditor()?.getValue() && this.updateMonacoVal(val);
        },

        editorName() {
            return `we-editor-${this.type}`;
        },
        currentConfig() {
            const typeConfig = types[this.type];
            const config = merge(
                {
                    automaticLayout: false,
                    scrollBeyondLastLine: false,
                    minimap: {
                        enabled: false,
                    },
                    readOnly: this.readOnly,
                    glyphMargin: true,
                },
                typeConfig,
                this.options,
                {
                    value: this.value,
                    theme: this.theme,
                    language: this.language,
                },
            );
            return config;
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
            editorModel: null,
            decorations: null,
            isParserClose: true, // Syntax validation is turned off by default(默认关闭语法验证)
            closeParser: null,
            openParser: null,
            sqlParser: null,
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
        initMonaco() {
            this.editor = monaco.editor.create(this.$el, this.currentConfig);
            this.monaco = monaco;
            this.editorModel = this.editor.getModel();
            if (this.type !== 'log') {
                if (this.scriptType !== 'hdfsScript' && !this.readOnly) {
                    this.addCommands();
                    this.addActions();
                }
                if (this.language === 'hql') {
                    this.initParser();
                } else {
                    this.deltaDecorations(this.value);
                }
            }
            this.$emit('onload');
            this.editor.onDidChangeModelContent(
                debounce(() => {
                    this.$emit('input', this.getValue());
                }),
                100,
            );
            this.editor.onContextMenu(
                debounce(() => {
                    // The right-click menu function that needs to change the text(需要调换文字的右键菜单功能)
                    const selectList = [
                        {
                            label: 'Change All Occurrences',
                            text: '改变所有出现',
                        },
                        { label: 'Format Document', text: '格式化' },
                        { label: 'Command Palette', text: '命令面板' },
                        { label: 'Cut', text: '剪切' },
                        { label: 'Copy', text: '复制' },
                    ];
                    if (localStorage.getItem('locale') === 'zh-CN') {
                        selectList.forEach((item) => {
                            const elmentList = document.querySelectorAll(
                                `.actions-container .action-label[aria-label="${item.label}"]`,
                            );
                            this.changeInnerText(elmentList, item.text);
                        });
                    }
                }),
                100,
            );
        },

        undo() {
            this.editor.trigger('anyString', 'undo');
        },
        redo() {
            this.editor.trigger('anyString', 'redo');
        },
        // save the current value(保存当前的值)
        save() {
            if (this.editorModel) {
                this.deltaDecorations();
            }
        },
        // Saved edit state ViewState(保存的编辑状态 ViewState)
        saveViewState() {
            if (this.editorModel) {
                this.editorModel.viewState = this.editor.saveViewState();
            }
        },
        // Reset the previously saved edit state ViewState(重置之前保存的编辑状态 ViewState)
        restoreViewState() {
            if (this.editorModel && this.editorModel.viewState) {
                this.editor.restoreViewState(this.editorModel.viewState);
            }
        },
        // Get editor content(获取编辑器内容)
        getValue() {
            return this.editor.getValue({
                lineEnding: '\n',
                preserveBOM: false,
            });
        },
        // Get selected content(获取选择的内容)
        getValueInRange() {
            const selection = this.editor.getSelection();
            return selection.isEmpty()
                ? null
                : this.editor.getModel().getValueInRange(selection);
        },
        // Insert a value in the selected range in the editor(在编辑器选中的范围插入值)
        insertValueIntoEditor(value) {
            if (this.editor) {
                const SelectedRange = this.editor.getSelection();
                let range = null;
                if (SelectedRange) {
                    range = new monaco.Range(
                        SelectedRange.startLineNumber,
                        SelectedRange.startColumn,
                        SelectedRange.endLineNumber,
                        SelectedRange.endColumn,
                    );
                    const text = value;
                    const op = {
                        identifier: {
                            major: 1,
                            minor: 1,
                        },
                        range,
                        text,
                        forceMoveMarkers: true,
                    };
                    this.editor.executeEdits('insertValue', [op]);
                }
            }
        },
        addCommands() {
            // save the current script(保存当前脚本)
            this.editor.addCommand(
                monaco.KeyMod.CtrlCmd + monaco.KeyCode.KEY_S,
                () => {
                    this.$emit('on-save');
                },
            );
            // run the current script(运行当前脚本)
            if (this.executable) {
                this.editor.addCommand(monaco.KeyCode.F3, () => {
                    this.$emit('on-run');
                });
            }
            // Invokes the convert lowercase action of the browser itself(调用浏览器本身的转换小写动作)
            this.editor.addCommand(
                monaco.KeyMod.CtrlCmd +
                    monaco.KeyMod.Shift +
                    monaco.KeyCode.KEY_U,
                () => {
                    this.editor.trigger(
                        'toLowerCase',
                        'editor.action.transformToLowercase',
                    );
                },
            );
            // Invokes the convert caps action of the browser itself(调用浏览器本身的转换大写动作)
            this.editor.addCommand(
                monaco.KeyMod.CtrlCmd + monaco.KeyCode.KEY_U,
                () => {
                    this.editor.trigger(
                        'toUpperCase',
                        'editor.action.transformToUppercase',
                    );
                },
            );
        },
        addActions() {
            const vm = this;

            this.editor.addAction({
                id: 'editor.action.execute',
                label: this.$t('message.common.monacoMenu.YXJB'),
                keybindings: [monaco.KeyCode.F3],
                keybindingContext: null,
                contextMenuGroupId: 'navigation',
                contextMenuOrder: 1.5,
                run() {
                    vm.$emit('on-run');
                },
            });

            // this.editor.addAction({
            //   id: 'format',
            //   label: this.$t('message.common.monacoMenu.GSH'),
            //   keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyMod.Shift | monaco.KeyCode.KEY_L],
            //   keybindingContext: null,
            //   contextMenuGroupId: 'control',
            //   contextMenuOrder: 1.5,
            //   run(editor) {
            //     editor.trigger('anyString', 'editor.action.formatDocument');
            //   },
            // });

            this.editor.addAction({
                id: 'find',
                label: this.$t('message.common.monacoMenu.CZ'),
                keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_F],
                keybindingContext: null,
                contextMenuGroupId: 'control',
                contextMenuOrder: 1.6,
                run(editor) {
                    editor.trigger('find', 'actions.find');
                },
            });

            this.editor.addAction({
                id: 'replace',
                label: this.$t('message.common.monacoMenu.TH'),
                keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_H],
                keybindingContext: null,
                contextMenuGroupId: 'control',
                contextMenuOrder: 1.7,
                run(editor) {
                    editor.trigger(
                        'findReplace',
                        'editor.action.startFindReplaceAction',
                    );
                },
            });

            this.editor.addAction({
                id: 'commentLine',
                label: this.$t('message.common.monacoMenu.HZS'),
                keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.US_SLASH],
                keybindingContext: null,
                contextMenuGroupId: 'control',
                contextMenuOrder: 1.8,
                run(editor) {
                    editor.trigger('commentLine', 'editor.action.commentLine');
                },
            });

            this.editor.addAction({
                id: 'paste',
                label: this.$t('message.common.monacoMenu.ZT'),
                keybindings: [],
                keybindingContext: null,
                contextMenuGroupId: '9_cutcopypaste',
                contextMenuOrder: 2,
                run() {
                    const copyString = storage.get('copyString');
                    if (!copyString || copyString.length < 0) {
                        vm.$Message.warning(
                            this.$t('message.common.monacoMenu.HBQWJCFZWB'),
                        );
                    } else {
                        vm.insertValueIntoEditor(copyString);
                    }
                    return null;
                },
            });

            this.editor.addAction({
                id: 'gotoLine',
                label: this.$t('message.common.monacoMenu.TDZDH'),
                keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_G],
                keybindingContext: null,
                contextMenuGroupId: 'control',
                contextMenuOrder: 1.9,
                run(editor) {
                    editor.trigger('gotoLine', 'editor.action.gotoLine');
                },
            });

            if (this.language === 'hql') {
                // Control grammar check(控制语法检查)
                this.closeParser = this.editor.createContextKey(
                    'closeParser',
                    !this.isParserClose,
                );
                this.openParser = this.editor.createContextKey(
                    'openParser',
                    this.isParserClose,
                );
                this.editor.addAction({
                    id: 'closeParser',
                    label: this.$t('message.common.monacoMenu.GBYFJC'),
                    keybindings: [],
                    keybindingContext: null,
                    // Used to control the display of the right-click menu(用于控制右键菜单的显示)
                    precondition: 'closeParser',
                    contextMenuGroupId: 'control',
                    contextMenuOrder: 2.0,
                    run() {
                        vm.isParserClose = true;
                        // Controls the display of the right-click menu(控制右键菜单的显示)
                        vm.openParser.set(true);
                        vm.closeParser.set(false);
                        vm.deltaDecorations();
                    },
                });

                this.editor.addAction({
                    id: 'openParser',
                    label: this.$t('message.common.monacoMenu.DKYFJC'),
                    keybindings: [],
                    keybindingContext: null,
                    precondition: 'openParser',
                    contextMenuGroupId: 'control',
                    contextMenuOrder: 2.1,
                    run() {
                        vm.isParserClose = false;
                        vm.openParser.set(false);
                        vm.closeParser.set(true);
                        vm.deltaDecorations();
                    },
                });
            }
        },
        deltaDecorations: debounce(function (value, cb) {
            // eslint-disable-next-line @typescript-eslint/no-this-alias
            const vm: any = this;
            if (!vm.isParserClose) {
                let highRiskList = [];
                const lang = vm.language;
                const app = vm.application;
                if (
                    lang === 'python' ||
                    (app === 'spark' && ['java', 'hql'].indexOf(lang) !== -1) ||
                    app === 'hive'
                ) {
                    // High-risk syntax highlighting(高危语法的高亮)
                    highRiskList = vm.setHighRiskGrammar();
                    const decora = vm.decorations || [];
                    let isParseSuccess = true;
                    if (lang === 'hql') {
                        const val = value || vm.value;
                        const validParser = !vm.sqlParser
                            ? null
                            : vm.sqlParser.parser.parseSyntax(val, 'hive');
                        let newDecora = [];
                        if (validParser) {
                            isParseSuccess = false;
                            const warningLalbel = `编译语句时异常：在行${validParser.loc.first_line}:${validParser.loc.first_column}，输入词'${validParser.text}'附近可能存在sql语法错误`;
                            const range = new monaco.Range(
                                validParser.loc.first_line,
                                validParser.loc.first_column,
                                validParser.loc.last_line,
                                validParser.loc.last_column + 1,
                            );
                            // The first element is highlighting the wrong keywords, the second is highlighting lines and margin blocks(第一个元素是对错误的关键词做高亮的，第二个元素是对行和margin块做高亮)的
                            newDecora = [
                                {
                                    range,
                                    options: {
                                        inlineClassName: 'inlineDecoration',
                                    },
                                },
                                {
                                    range,
                                    options: {
                                        isWholeLine: true,
                                        className: 'contentClass',
                                        // To use color blocks in margin, the editor must set glyphMargin to true(要在margin中使用色块，编辑器必须将glyphMargin设置为true)
                                        glyphMarginClassName:
                                            'glyphMarginClass',
                                        // hover prompt, the use of glyphHoverMessage in the documentation is wrong(hover提示，文档中使用glyphHoverMessage是错的)
                                        hoverMessage: {
                                            value: warningLalbel,
                                        },
                                    },
                                },
                            ];
                        }
                        // The first parameter is old, used to clear decorations(第一个参数是旧的，用于清空decorations)
                        vm.decorations = vm.editor.deltaDecorations(
                            decora,
                            newDecora.concat(highRiskList),
                        );
                        vm.$emit('is-parse-success', isParseSuccess);
                    } else {
                        vm.decorations = vm.editor.deltaDecorations(
                            decora,
                            highRiskList,
                        );
                    }
                    if (cb) {
                        cb(isParseSuccess);
                    }
                } else if (cb) {
                    cb(true);
                }
            } else {
                // When the grammar check is turned off, if there is an error color block on the editor, clear it first(关闭语法检查时，如果编辑器上有错误色块，先清除)
                const decora = vm.decorations || [];
                vm.decorations = vm.editor.deltaDecorations(decora, []);
                if (cb) {
                    cb(true);
                }
            }
        }, 500),
        setHighRiskGrammar() {
            let highRiskList = [];
            const HOVER_MESSAGE = this.$t(
                'message.common.monacoMenu.GYFSYGWYF',
            );
            let highRiskGrammarToken = highRiskGrammar[this.language];
            if (this.language === 'java') {
                highRiskGrammarToken = highRiskGrammar.scala;
            }
            if (highRiskGrammarToken && highRiskGrammarToken.length) {
                // Because there are multiple regular tokens, use all the rules to traverse(因为正则的token有多个，所以要用所有的规则去遍历)
                highRiskGrammarToken.forEach((key) => {
                    // Use regular to grab the matching text in the editor, which is an array(使用正则去抓取编辑器中的匹配文本，是一个数组)
                    const errorLabel = this.editorModel.findMatches(
                        key,
                        false,
                        true,
                        false,
                        null,
                        true,
                    );
                    if (errorLabel.length) {
                        const formatedRiskGrammer = [];
                        errorLabel.forEach((item) => {
                            // Get the content of the current entire line(拿到当前整行的内容)
                            const lineContent = this.editorModel.getLineContent(
                                item.range.startLineNumber,
                            );
                            let reg = /^[^\-\-]\s*/;
                            if (this.language === 'python') {
                                reg = /^[^\#]\s*/;
                            } else if (this.language === 'java') {
                                reg = /^[^\/\/]\s*/;
                            }
                            const isHasComment = reg.test(lineContent);
                            // Determine if there are annotations(判断是否有注释)
                            if (isHasComment) {
                                formatedRiskGrammer.push({
                                    range: item.range,
                                    options: {
                                        inlineClassName: 'highRiskGrammar',
                                        hoverMessage: {
                                            value: HOVER_MESSAGE,
                                        },
                                    },
                                });
                            }
                        });
                        highRiskList = highRiskList.concat(formatedRiskGrammer);
                    }
                });
            }
            return highRiskList;
        },
        async initParser() {
            this.monaco.editor.setModelLanguage(
                this.editorModel,
                this.language,
            );
            // if (this.language === 'hql' && !this.sqlParser) {
            //     this.sqlParser = await import('dt-sql-parser');
            // }
            this.deltaDecorations(this.value);
        },
    },
    mounted() {
        console.log(this.$el, this.$props);
        if (this.$el) {
            // this.editor = monaco.editor.create(this.$el, this.currentConfig);
            // this.monaco = monaco;
            // this.editorModel = this.editor.getModel();

            this.editor = this.createEditor(
                this.$el,
                this.$props.editorOptions,
            );
            this.updateMonacoVal();
            this.editor!.onDidChangeModelContent(() => {
                this.$emit('update:modelValue', monacoEditor!.getValue());
            });
            this.editor!.onDidBlurEditorText(() => {
                this.$emit('blur');
            });
        }
    },
    beforeUnmount() {
        // 销毁 editor，进行gc(销毁 editor，进行gc)
        this.editor!.dispose();
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

    // &.full {
    //     position: fixed;
    //     left: calc(10vw / 2);
    //     top: calc(10vh / 2);
    //     box-shadow: 0 0 22px 10px rgba(0, 0, 0, 0.3);
    //     width: 90vw !important;
    //     height: 90vh !important;
    //     z-index: 9999;
    // }
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
