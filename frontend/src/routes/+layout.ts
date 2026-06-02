/**
 * This file gets executed whenever a page is loaded. It fetches the available ETCS-Modules
 * from the backend.
 */
// Svelte
import type {PageLoad} from "../../.svelte-kit/types/src/routes/$types";
import {building} from "$app/environment";

// URLs
import {scanFileSystemUrl} from "../constants/backendURLs/moduleManagerURLs";

// Stores
import {appState} from "../stores/pageState";

// Utils
import {updateAvailableModules} from "../utils/backendUtils";

// Constants
import {backendConnectRetryInterval, maximumRetriesForBackendConnection} from "../constants/configVariables";

/**
 * Ensures that all pages get prerendered when building the svelte-kit project.
 * This is necessary for the electron process. It loads prerendered html pages when executed.
 */
export const prerender = true;

// @ts-ignore
export const load: PageLoad = async ({fetch}) => {
    if (!building) {
        let state = 1;
        appState.subscribe(pageState => {
            state = pageState;
        });
        if (state === 1) {
            appState.set(1);
            let retryCount = 0;

            const makeRequest = async () => {
                const retryInterval = setInterval(async () => {
                    try {
                        const response = await fetch(scanFileSystemUrl, {method: 'POST'});
                        const availableModulesData = await response.json();
                        updateAvailableModules(availableModulesData);
                        appState.set(0);
                        clearInterval(retryInterval);
                    } catch (error) {
                        retryCount++;
                        if (retryCount >= maximumRetriesForBackendConnection) {
                            appState.set(2);
                            clearInterval(retryInterval);
                        }
                    }
                }, backendConnectRetryInterval);
            };
            await makeRequest();
        }
    }
};