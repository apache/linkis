import { ref } from 'vue';

export function useInstance() {
    const refEl = ref();
    const proxy = new Proxy(
        {},
        {
            get(_, key) {
                if (
                    typeof refEl.value?.[key] === 'function' &&
                    refEl.value.hasOwnProperty &&
                    !refEl.value.hasOwnProperty(key)
                ) {
                    return refEl.value[key].bind(refEl.value);
                }
                return refEl.value?.[key];
            },
            set(_, key, value) {
                if (refEl.value) refEl.value[key] = value;
            },
        },
    );
    return [refEl, proxy];
}
