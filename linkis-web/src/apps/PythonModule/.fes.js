import { defineBuildConfig } from '@fesjs/fes';
export default defineBuildConfig({
    // base: './',
    proxy: {
        base: './'
    },
    viteOption: {
        build: {
            outDir: '../../../dist/dist/dist'
        }
    }
    
});
