import { createApp } from 'vue';
import '@/style/style.less';
import {
    FTabs,
    FSwitch,
    FCheckbox,
    FModal,
    FSpin,
    FButton,
    FInput,
    FInputNumber,
    FForm,
    FTag,
    FSelect,
    FTable,
    FPagination,
    FDatePicker,
    FDropdown,
    FDrawer,
    FCheckboxGroup,
    FSelectTree,
    FSelectCascader,
    FTooltip,
    FSpace,
    FRadio,
    FRadioGroup,
    FEllipsis,
    FMenu,
    FLayout,
    FTimePicker
} from '@fesjs/fes-design';
import App from '@/App.vue';
import router from '@/router/index.ts';

const app = createApp(App);
app.use(router).mount('#app');
app.use(FButton);
app.use(FTabs);
app.use(FSelect);
app.use(FSwitch);
app.use(FCheckbox);
app.use(FCheckboxGroup);
app.use(FModal);
app.use(FInput);
app.use(FInputNumber);
app.use(FForm);
app.use(FTag);
app.use(FSpin);
app.use(FTable);
app.use(FPagination);
app.use(FDatePicker);
app.use(FDropdown);
app.use(FDrawer);
app.use(FSelectTree);
app.use(FSelectCascader);
app.use(FEllipsis);
app.use(FSpace);
app.use(FTooltip);
app.use(FRadioGroup);
app.use(FRadio);
app.use(FLayout);
app.use(FMenu);
app.use(FTimePicker);
