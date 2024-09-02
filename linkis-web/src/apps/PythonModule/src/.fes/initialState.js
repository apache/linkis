import { reactive } from 'vue';

export const initialState = reactive({});

export const updateInitialState = (obj) => {
    Object.assign(initialState, obj);
};
