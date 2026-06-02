import {defineConfig} from 'vitest/config'
import * as path from "path";

export default defineConfig({
    resolve: {
        alias: {
            '$lib': path.resolve(__dirname, './src/lib')
        },
    },
    test: {
        include: ['**/*.vtest.ts'],
    },
});