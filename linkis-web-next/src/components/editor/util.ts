/* eslint-disable import/no-extraneous-dependencies */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import monaco from './monaco-loader';

export const useMonaco = (language: string) => {
    let monacoEditor: monaco.editor.IStandaloneCodeEditor | null = null;
    let initReadOnly = true;
    const updateVal = async (val: string) => {
        monacoEditor?.setValue(val);
        setTimeout(async () => {
            if (initReadOnly) monacoEditor?.updateOptions({ readOnly: false });
            await monacoEditor
                ?.getAction?.('editor.action.formatDocument')
                ?.run();
            if (initReadOnly) monacoEditor?.updateOptions({ readOnly: true });
        }, 100);
    };

    const createEditor = (
        el: HTMLElement | null,
        editorOption: monaco.editor.IStandaloneEditorConstructionOptions = {},
    ) => {
        if (monacoEditor) {
            return;
        }
        initReadOnly = !!editorOption.readOnly;
        monacoEditor =
            el &&
            monaco.editor.create(el, {
                language,
                minimap: { enabled: false },
                theme: 'logview',
                multiCursorModifier: 'ctrlCmd',
                scrollbar: {
                    verticalScrollbarSize: 8,
                    horizontalScrollbarSize: 8,
                },
                tabSize: 2,
                automaticLayout: true, // 自适应宽高
                unicodeHighlight: {
                    ambiguousCharacters: false
                },
                glyphMargin: false,
                selectOnLineNumbers: false,
                wordWrap: 'on',
                ...editorOption,
            });
        return monacoEditor;
    };
    const onFormatDoc = () => {
        monacoEditor?.getAction?.('editor.action.formatDocument')?.run();
    };
    return {
        updateVal,
        getEditor: () => monacoEditor,
        createEditor,
        onFormatDoc,
    };
};
