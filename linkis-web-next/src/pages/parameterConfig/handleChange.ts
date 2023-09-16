import { reactive } from 'vue';
import { data, currentData } from './drawer.vue';

export const handleChange = (currentPage, pageSize) => {
const temp = data.slice((currentPage - 1) * pageSize, currentPage * pageSize);
currentData = reactive(temp);
};
