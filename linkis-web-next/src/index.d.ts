export {};

declare global {
    interface Window {
        debug_log: boolean;
        console: Record<string, (...args: any[]) => unknown>;
        [key: string]: any;
    }
}
