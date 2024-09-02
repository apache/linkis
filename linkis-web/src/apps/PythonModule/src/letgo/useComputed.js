import { computed, reactive } from 'vue';

export function useComputed({ id, func }) {
    return reactive({
        id,
        value: computed(() => {
            try {
                return func();
            } catch (_) {
                window.console.warn(_);
                return null;
            }
        }),
    });
}
