/** @type {import('@playwright/test').PlaywrightTestConfig} */
import {devPort} from "./src/constants/configVariables";

const config = {
    webServer: {
        command: 'npm run dev:svelte',
        port: devPort
    }
};

export default config