import { onMounted, defineComponent } from 'vue';
import { defineRouteMeta } from '@fesjs/fes';
import { FForm, FFormItem, FButton, FModal, FText } from '@fesjs/fes-design';
import {
    WInput,
    WSelect,
    WRadioGroup,
    WUpload,
    WTextarea,
    WTable,
} from '@webank/fes-design-material';
import { useSharedLetgoGlobal } from '../../letgo/useLetgoGlobal';
import { useJSQuery } from '../../letgo/useJSQuery';
import { letgoRequest } from '../../letgo/letgoRequest';
import { Main } from './main';
import { markClassReactive } from '../../letgo/reactive.js';
import { isGetterProp } from '../../letgo/shared.js';

defineRouteMeta({
    name: 'index',
    title: 'agent生成',
});

export default defineComponent({
    name: 'index',
    setup() {
        const { $context, $utils } = useSharedLetgoGlobal();

        const apiPythonfileexistUdf = useJSQuery({
            id: 'apiPythonfileexistUdf',
            query(params) {
                return letgoRequest(
                    '/api/rest_j/v1/udf/python-file-exist',
                    params,
                    {
                        method: 'GET',
                        headers: {},
                    },
                );
            },

            runCondition: 'manual',

            successEvent: [],
            failureEvent: [],
        });

        const apiPythonuploadFilesystem = useJSQuery({
            id: 'apiPythonuploadFilesystem',
            query(params) {
                return letgoRequest(
                    '/api/rest_j/v1/filesystem/python-upload',
                    params,
                    {
                        method: 'POST'
                    },
                );
            },

            runCondition: 'manual',

            successEvent: [],
            failureEvent: [],
        });

        const apiPythonsaveUdf = useJSQuery({
            id: 'apiPythonsaveUdf',
            query(params) {
                return letgoRequest('/api/rest_j/v1/udf/python-save', params, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                });
            },

            runCondition: 'manual',

            successEvent: [],
            failureEvent: [],
        });

        const apiPythondeleteUdf = useJSQuery({
            id: 'apiPythondeleteUdf',
            query(params) {
                return letgoRequest(
                    '/api/rest_j/v1/udf/python-delete',
                    params,
                    {
                        method: 'GET',
                        headers: {},
                    },
                );
            },

            runCondition: 'manual',

            successEvent: [],
            failureEvent: [],
        });

        const apiPythonlistUdf = useJSQuery({
            id: 'apiPythonlistUdf',
            query(params) {
                return letgoRequest('/api/rest_j/v1/udf/python-list', params, {
                    method: 'GET',
                    headers: {},
                });
            },

            runCondition: 'manual',

            successEvent: [],
            failureEvent: [],
        });

        const __instance__ = new Main({
            globalCtx: {
                $utils,
                $context,
            },
            instances: {},
            codes: {
                apiPythonlistUdf,
                apiPythonfileexistUdf,
                apiPythonuploadFilesystem,
                apiPythonsaveUdf,
                apiPythondeleteUdf,
            },
        });
        const $$ = markClassReactive(__instance__, (member) => {
            if (
                member.startsWith('_') ||
                member.startsWith('$') ||
                isGetterProp(__instance__, member) ||
                typeof __instance__[member] === 'function'
            )
                return false;

            return true;
        });

        onMounted($$.onMounted.bind($$));

        const wTable2Columns9RenderSlots = (slotProps) => {
            return (
                <div style={{width: '200px', display: 'flex'}}>
                    <FButton
                        disabled={slotProps.row.isExpire === 1}
                        onClick={[
                            () => {
                                $$.showEditModuleModal(slotProps.row);
                            },
                        ]}
                        type="link"
                        style={{padding: '0px', width: '45px'}}
                    >
                        编辑
                    </FButton>
                    <FButton
                        disabled={slotProps.row.isExpire === 1}
                        onClick={[
                            () => {
                                $$.showLoadStatusChangeConfirmation(
                                    slotProps.row,
                                );
                            },
                        ]}
                        type="link"
                        style={{padding: '0px', width: '45px'}}
                    >
                        {slotProps.row.isLoad === 1 ? '已加载' : '未加载'}
                    </FButton>
                    <FButton
                        disabled={slotProps.row.isExpire === 1}
                        onClick={[
                            () => {
                                $$.showDeleteConfirmation(slotProps.row);
                            },
                        ]}
                        type="link"
                        style={{ color: slotProps.row.isExpire === 1 ? '#CFD0D3' : 'red', padding: '0px', width: '45px'}}
                    >
                        删除
                    </FButton>
                </div>
            );
        };

        return () => {
            return (
                <div
                    class="letgo-page"
                    style={{
                        padding: '10px',
                    }}
                >
                    <FForm labelWidth={80} layout={`inline`}>
                        <WInput
                            _label={`模块名称`}
                            placeholder={`请输入Python模块名称`}
                            v-model={$$.pythonModuleName}
                            span={5}
                            style={{
                                width: '260px',
                            }}
                            labelWidth={`60px`}
                        />
                        <WInput
                            _label={`用户名`}
                            placeholder={`请输入用户名`}
                            v-model={$$.userName}
                            span={5}
                            style={{
                                width: '260px',
                            }}
                            labelWidth={`60px`}
                        />
                        <WSelect
                            _label={`引擎类型`}
                            labelWidth={`60px`}
                            span={5}
                            style={{
                                width: '260px',
                            }}
                            v-model={$$.engineType}
                            options={[
                                {
                                    value: 'spark',
                                    label: 'Spark',
                                },
                                {
                                    value: 'python',
                                    label: 'Python',
                                },
                                {
                                    value: 'all',
                                    label: '通用',
                                },
                            ]}
                        />
                        <WSelect
                            _label={`是否过期`}
                            labelWidth={60}
                            span={5}
                            style={{
                                width: '260px',
                            }}
                            v-model={$$.isExpired}
                            options={[
                                {
                                    value: 0,
                                    label: '否',
                                },
                                {
                                    value: 1,
                                    label: '是',
                                },
                                {
                                    value: '',
                                    label: '全部',
                                }
                            ]}
                        />
                        <WSelect
                            _label={`是否加载`}
                            labelWidth={60}
                            span={5}
                            style={{
                                width: '260px',
                            }}
                            v-model={$$.isLoaded}
                            options={[
                                {
                                    value: 0,
                                    label: '否',
                                },
                                {
                                    value: 1,
                                    label: '是',
                                },
                                {
                                    value: '',
                                    label: '全部',
                                }
                            ]}
                        />
                        <FFormItem span={3} class={['buttons']}>
                            <FButton
                                type="primary"
                                onClick={[
                                    () => {
                                        $$.currentPage = 1
                                        $$.loadPythonModuleList(
                                            $$.pythonModuleName,
                                            $$.userName,
                                            $$.engineType,
                                            $$.isExpired,
                                            $$.isLoaded,
                                            $$.currentPage,
                                            $$.pageSize,
                                        );
                                    },
                                ]}
                            >
                                搜索
                            </FButton>
                            <FButton
                                onClick={[
                                    () => {
                                        $$.currentPage = 1;
                                        $$.resetQueryParameters();
                                    },
                                ]}
                                type="primary"
                                style={{ backgroundColor: '#ff9900', color: '#fff', borderColor: '#ff9900' }}
                            >
                                清空
                            </FButton>
                            <FButton
                                onClick={[
                                    () => {
                                        $$.showAddModuleModal();
                                    },
                                ]}
                                type="primary"
                                style={{ backgroundColor: '#19be6b', color: '#fff', borderColor: '#19be6b' }}
                            >
                                新增
                            </FButton>
                        </FFormItem>
                    </FForm>
                    <FModal
                        show={$$.editPythonModuleVisible}
                        title={`编辑Python模块`}
                        displayDirective={'show'}
                        closable={true}
                        mask={false}
                        maskClosable={true}
                        width={520}
                        top={50}
                        verticalCenter={false}
                        center={false}
                        fullScreen={false}
                        footer={true}
                        okText={'确认'}
                        okLoading={false}
                        cancelText={'取消'}
                        contentClass={''}
                        getContainer={() => document.body}
                        onUpdate:show={[
                            () => {
                                $$.closeEditModuleModal();
                            },
                        ]}
                        onOk={[
                            () => {
                                $$.handleEditModule();
                            },
                        ]}
                        onCancel={[
                            () => {
                                $$.closeEditModuleModal();
                            },
                        ]}
                    >
                        <FForm ref={(el) => $$.editFormRef = el} model={$$.selectedModule} labelWidth={80}>
                            <WInput
                                _label={`模块名称`}
                                v-model={$$.selectedModule.name}
                                rules={[
                                    {
                                        required: true,
                                        message: '模块名称必填',
                                    },
                                ]}
                                placeholder={`请输入模块名称`}
                                disabled
                            />
                            <WRadioGroup
                                _label={`引擎类型`}
                                v-model={$$.selectedModule.engineType}
                                rules={[
                                    {
                                        required: true,
                                        message: '请选择引擎类型',
                                    },
                                ]}
                                options={[
                                    {
                                        value: 'spark',
                                        label: 'Spark',
                                    },
                                    {
                                        value: 'python',
                                        label: 'Python',
                                    },
                                    {
                                        value: 'all',
                                        label: '通用',
                                    },
                                ]}
                            />
                            <WUpload
                                v-model:fileList={$$.selectedModule.fileList}
                                _label={`模块物料`}
                                action={`/api/rest_j/v1/udf/python-upload`}
                                rules={[
                                    {
                                        required: true,
                                        message: '请上传模块物料',
                                        type: 'array',
                                        validator: (rule, value) => {
                                            if($$.selectedModule.path && $$.selectedModule.name) {
                                                return true;
                                            }
                                            return false
                                        }
                                    },
                                ]}
                                v-slots={{
                                    tip: () => {
                                        if(!$$.selectedModule.name) {
                                            return (
                                                <FText
                                                    type={'default'}
                                                    style={{
                                                        width: '200px',
                                                    }}
                                                >
                                                    {'仅支持上传 .py和.zip格式文件'}
                                                </FText>
                                            );
                                        }
                                        else {
                                            return ''
                                        }
                                        
                                    },
                                    fileList: () => {
                                        if($$.selectedModule.name && $$.selectedModule.path) {
                                            return (
                                                <div>
                                                    <FText type={'default'}>{$$.selectedModule.name + '.' + $$.selectedModule.path?.split('.')[1] || ''}</FText>
                                                </div>
                                            );
                                        }
                                        else {
                                            return ''
                                        }
                                        
                                    }
                                }}
                                beforeUpload={(...args) => $$.validateModuleFile(...args)}
                                accept={['.zip', '.py']}
                                httpRequest={(...args) => $$.handleUploadHttpRequest(...args)}
                            />
                            <WRadioGroup
                                _label={`是否加载`}
                                v-model={$$.selectedModule.isLoad}
                                rules={[
                                    {
                                        required: true,
                                        message: '请选择是否加载',
                                        type: 'number',
                                    },
                                ]}
                                options={[
                                    {
                                        value: 1,
                                        label: '是',
                                    },
                                    {
                                        value: 0,
                                        label: '否',
                                    },
                                ]}
                            />
                            <WRadioGroup
                                _label={`是否过期`}
                                v-model={$$.selectedModule.isExpire}
                                rules={[
                                    {
                                        required: true,
                                        message: '请选择是否过期',
                                        type: 'number',
                                    },
                                ]}
                                options={[
                                    {
                                        value: 1,
                                        label: '是',
                                    },
                                    {
                                        value: 0,
                                        label: '否',
                                    },
                                ]}
                            />
                            <WInput
                                _label={`模块描述`}
                                v-model={$$.selectedModule.description}
                                rules={[
                                    {
                                        required: false,
                                        message: '模块描述非必填',
                                    },
                                ]}
                                type="textarea"
                                placeholder={`请输入模块描述`}
                                autosize={{minRows: 2, maxRows: 5}}
                            />
                        </FForm>
                    </FModal>
                    <FModal
                        show={$$.addPythonModuleVisible}
                        mask={false}
                        title={`新增Python模块`}
                        contentClass={
                            $$.addModuleModalVisible ? 'custom-class' : ''
                        }
                        onUpdate:show={[
                            () => {
                                $$.closeAddModuleModal();
                            },
                        ]}
                        onOk={() => {
                            $$.handleAddModule();
                        }}
                    >
                        <FForm ref={(el) => $$.addFormRef = el} model={$$.selectedModule} labelWidth={80}>
                            <WInput
                                _label={`模块名称`}
                                v-model={$$.selectedModule.name}
                                rules={[
                                    {
                                        required: true,
                                        message: '模块名称必填',
                                    },
                                ]}
                                placeholder={`请输入模块名称`}
                                disabled
                            />
                            <WRadioGroup
                                _label={`引擎类型`}
                                v-model={$$.selectedModule.engineType}
                                rules={[
                                    {
                                        required: true,
                                        message: '请选择引擎类型',
                                    },
                                ]}
                                options={[
                                    {
                                        value: 'spark',
                                        label: 'Spark',
                                    },
                                    {
                                        value: 'python',
                                        label: 'Python',
                                    },
                                    {
                                        value: 'all',
                                        label: '通用',
                                    },
                                ]}
                            />
                            <WUpload
                                v-model:fileList={$$.selectedModule.fileList}
                                _label={`模块物料`}
                                action={`/api/rest_j/v1/udf/python-upload`}
                                rules={[
                                    {
                                        required: true,
                                        message: '请上传模块物料',
                                        type: 'array',
                                        validator:(rule, value) => {
                                            if($$.selectedModule.path && $$.selectedModule.name) {
                                                return true;
                                            }
                                            return false
                                        }
                                    },
                                ]}
                                v-slots={{
                                    tip: () => {
                                        if(!$$.selectedModule.name) {
                                            return (
                                                <FText
                                                    type={'default'}
                                                    style={{
                                                        width: '200px',
                                                    }}
                                                >
                                                    {'仅支持上传 .py和.zip格式文件'}
                                                </FText>
                                            );
                                        }
                                        else {
                                            return ''
                                        }
                                        
                                    },
                                    fileList: () => {
                                        if($$.selectedModule.name && $$.selectedModule.path) {
                                            return (
                                                <div>
                                                    <FText type={'default'}>{$$.selectedModule.name + '.' + $$.selectedModule.path?.split('.')[1] || ''}</FText>
                                                </div>
                                            );
                                        }
                                        else {
                                            return ''
                                        }
                                        
                                    }
                                }}
                                beforeUpload={(...args) => $$.validateModuleFile(...args)}
                                accept={['.zip', '.py']}
                                httpRequest={(...args) => $$.handleUploadHttpRequest(...args)}
                            />
                            <WRadioGroup
                                _label={`是否加载`}
                                v-model={$$.selectedModule.isLoad}
                                rules={[
                                    {
                                        required: true,
                                        message: '请选择是否加载',
                                        type: 'number',
                                    },
                                ]}
                                options={[
                                    {
                                        value: 1,
                                        label: '是',
                                    },
                                    {
                                        value: 0,
                                        label: '否',
                                    },
                                ]}
                            />
                            <WRadioGroup
                                _label={`是否过期`}
                                v-model={$$.selectedModule.isExpire}
                                rules={[
                                    {
                                        required: true,
                                        message: '请选择是否过期',
                                        type: 'number',
                                    },
                                ]}
                                options={[
                                    {
                                        value: 1,
                                        label: '是',
                                    },
                                    {
                                        value: 0,
                                        label: '否',
                                    },
                                ]}
                            />
                            <WInput
                                _label={`模块描述`}
                                v-model={$$.selectedModule.description}
                                rules={[
                                    {
                                        required: false,
                                        message: '模块描述非必填',
                                    },
                                ]}
                                type="textarea"
                                placeholder={`请输入模块描述`}
                                autosize={{minRows: 2, maxRows: 5}}
                            />
                        </FForm>
                    </FModal>
                    <WTable
                        columns={[
                            {
                                prop: 'name',
                                label: '模块名称',
                            },
                            {
                                prop: 'engineType',
                                label: '引擎类型',
                                formatter: ({
                                    row,
                                    column,
                                    rowIndex,
                                    coloumIndex,
                                    cellValue,
                                }) => {
                                    return row.engineType === 'all'
                                        ? '通用' : row.engineType === 'spark' ? 'Spark'
                                        : 'Python';
                                },
                            },
                            {
                                prop: 'status',
                                label: '状态',
                                formatter: ({
                                    row,
                                    column,
                                    rowIndex,
                                    coloumIndex,
                                    cellValue,
                                }) => {
                                    return row.isExpire === 0
                                        ? '正常'
                                        : '过期';
                                },
                            },
                            {
                                prop: 'path',
                                label: '路径信息',
                            },
                            {
                                prop: 'description',
                                label: '模块描述',
                            },
                            {
                                prop: 'createTime',
                                label: '创建时间',
                                formatter: ({
                                    row,
                                    column,
                                    rowIndex,
                                    columnIndex,
                                    cellValue,
                                }) => {
                                    return $utils
                                        .dayjs(row.createTime)
                                        .format('YYYY-MM-DD HH:mm:ss');
                                },
                            },
                            {
                                prop: 'updateTime',
                                label: '修改时间',
                                formatter: ({
                                    row,
                                    column,
                                    rowIndex,
                                    columnIndex,
                                    cellValue,
                                }) => {
                                    return $utils
                                        .dayjs(row.updateTime)
                                        .format('YYYY-MM-DD HH:mm:ss');
                                },
                            },
                            {
                                prop: 'createUser',
                                label: '创建人',
                            },
                            {
                                prop: 'updateUser',
                                label: '修改人',
                            },
                            {
                                prop: '',
                                label: '操作',
                                width: 200,
                                render: wTable2Columns9RenderSlots,
                            },
                        ]}
                        data={$$.pythonModuleList}
                        pagination={{
                            currentPage: $$.currentPage,
                            pageSize: $$.pageSize,
                            totalCount: $$.totalRecords,
                            showTotal: true,
                            alwayShow: true,
                            showQuickJumper: true,
                        }}
                        onChange={[(...args) => $$.handlePageChange(...args)]}
                    />
                </div>
            );
        };
    },
});
