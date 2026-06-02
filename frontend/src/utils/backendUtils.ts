// URLs
import {
    getRunningModulesUrl,
    sendInputUrl,
    startModulesUrl,
    stopModulesUrl
} from "../constants/backendURLs/processManagerURLs";
import {
    addAvailableModuleUrl,
    changeConfigValueUrl,
    deleteAvailableModuleUrl,
    getAvailableModuleUrl,
    getAvailableUrl
} from "../constants/backendURLs/moduleManagerURLs";

// Enums
import type {ETCSModuleNames} from "../constants/ETCSModuleNames";

// Stores
import {availableModules} from "../stores/modulesStore";

// Types
import type {ModuleInfo, ModuleInfos} from "../global";

/**
 * Fetches a URL from the Backend API.
 * @param url the URL to fetch
 * @param options options like the request method, body of the request etc.
 */
async function handleFetch(url: string, options: RequestInit): Promise<Response> {
    const response = await fetch(url, options);

    if (!response.ok) {
        throw new Error(`Error fetching data from ${url}: ${response.status} ${response.statusText}`);
    }

    return response;
}

export const updateAvailableModules = (fetchedAvailableModules: ModuleInfo[]) => {
    const newAvailableModules: ModuleInfos = {};
    fetchedAvailableModules.forEach((availableModule: ModuleInfo) => {
        const jarName: string = availableModule.jarName;
        const moduleName: ETCSModuleNames = availableModule.moduleName;
        const version: string = availableModule.version;
        const configDirectory: string = availableModule.configDirectory;
        const commandLineArguments: string[] = availableModule.commandLineArguments;
        const configValues: Record<string, string>[] = [];

        Object.keys(availableModule.configValues).forEach(variableName => {
            configValues.push({variableName: variableName, value: availableModule.configValues[variableName]});
        });

        newAvailableModules[moduleName.toString() + version] = {
            jarName: jarName,
            moduleName: moduleName,
            version: version,
            configDirectory: configDirectory,
            commandLineArguments: commandLineArguments,
            configValues: configValues
        }
    });
    availableModules.set(newAvailableModules);
};

export const getAvailableModules = async () => {
    const response = await handleFetch(getAvailableUrl, {method: 'GET'});
    const availableModulesData = await response.json();
    updateAvailableModules(availableModulesData);
};

export const getAvailableModule = async (moduleName: ETCSModuleNames, moduleVersion: string) => {
    const requestURL = getAvailableModuleUrl(moduleName, moduleVersion);
    const response = await handleFetch(requestURL, {method: 'GET'});
    const availableModuleData = await response.json();
    const configValues: Record<string, string>[] = [];

    Object.keys(availableModuleData.configValues).forEach(variableName => {
        configValues.push({variableName: variableName, value: availableModuleData.configValues[variableName]});
    });

    availableModules.update(availableModules => {
        availableModules[moduleName.toString() + moduleVersion] = {
            jarName: availableModuleData.jarName,
            moduleName: availableModuleData.moduleName,
            version: availableModuleData.version,
            configDirectory: availableModuleData.configDirectory,
            commandLineArguments: availableModuleData.commandLineArguments,
            configValues: configValues
        };

        return availableModules;
    })
};

export const getRunningModules = async () => {
    const response = await handleFetch(getRunningModulesUrl, {method: 'GET'});
    return await response.json();
}

export const addAvailableModule = async (moduleName: ETCSModuleNames, moduleVersion: string, pathToJar: string) => {
    const requestURL = addAvailableModuleUrl(moduleName, moduleVersion, pathToJar);
    await handleFetch(requestURL, {method: 'PUT'});
    return await getAvailableModule(moduleName, moduleVersion);
};

export const deleteAvailableModule = async (moduleName: ETCSModuleNames, moduleVersion: string) => {
    const requestURL = deleteAvailableModuleUrl(moduleName, moduleVersion);
    await handleFetch(requestURL, {method: 'DELETE'});
    return await getAvailableModules();
};

/**
 * Starts the specified ETCS-Modules.
 * @param modulesToRun the ETCS-Modules to start, containing the names of the ETCS-Modules as keys and their versions as values
 */
export const startModules = async (modulesToRun: Map<ETCSModuleNames, string>) => {
    const headers = {
        'Content-Type': 'application/json'
    };
    const body = Object.fromEntries(modulesToRun);

    return await handleFetch(startModulesUrl, {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(body)
    });
};

export const stopRunningModules = async () => {
    return await handleFetch(stopModulesUrl, {method: 'POST'});
};

export const sendInput = async (moduleName: ETCSModuleNames, input: string) => {
    const requestURL = sendInputUrl(moduleName, input);
    return await handleFetch(requestURL, {method: 'POST'});
};


/**
 * Changes the config value of an ETCS-Module.
 * @param moduleName the name of the ETCS-Module for which to change the config value
 * @param moduleVersion the version of the ETCS-Module for which to change the config value
 * @param variableName the name of the variable to change
 * @param newValue the new value of the variable
 */
export const changeConfigValue = async (moduleName: ETCSModuleNames, moduleVersion: string, variableName: string, newValue: string) => {
    const requestURL = changeConfigValueUrl(moduleName, moduleVersion, variableName, newValue);
    await handleFetch(requestURL, {method: 'PUT'});
    await getAvailableModule(moduleName, moduleVersion);
};