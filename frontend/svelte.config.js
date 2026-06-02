import {vitePreprocess} from '@sveltejs/vite-plugin-svelte';
import adapter from "@ptkdev/sveltekit-electron-adapter";


/** @type {import('@sveltejs/kit').Config} */
const config = {
    extensions: ['.svelte'],
    // Consult https://kit.svelte.dev/docs/integrations#preprocessors
    // for more information about preprocessors
    preprocess: [vitePreprocess()],

    kit: {
        adapter: adapter({strict: false})
    }
};
export default config;