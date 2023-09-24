import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';
// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd());
    return {
        envPrefix: 'VITE_',
        plugins: [vue()],
        resolve: {
            alias: {
                '@': path.resolve(__dirname, 'src'),
            },
        },
        server: {
            port: 3000,
            host: '127.0.0.1',
            proxy: {
                '/api': {
                    target: env.BACKEND_URL,
                    changeOrigin: true,
                    rewrite: (pth) => pth.replace(/^\/api/, ''),
                },
            },
        },
    };
});
