import { reactive } from 'vue';
import { isPlainObject, set } from 'lodash-es';

export function useTemporaryState({ id, initValue }) {
    const result = reactive({
        id,
        value: initValue,
        setValue,
        setIn,
    });

    function setIn(path, val) {
        if (isPlainObject(result.value)) {
            set(result.value, path, val);
        }
    }

    function setValue(val) {
        result.value = val;
    }

    return result;
}
