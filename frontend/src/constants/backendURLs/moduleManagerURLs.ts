import type {ETCSModuleNames} from "../ETCSModuleNames";
import {backendIP, backendPort} from "../configVariables";

export const moduleManagerUrl = `http://${backendIP}:${backendPort}/moduleManager`;


// ------  GET ------
export const getAvailableUrl = `${moduleManagerUrl}/availableModules`;
export const getAvailableModuleUrl = (moduleName: ETCSModuleNames, moduleVersion: string) => `${getAvailableUrl}/${moduleName}/${moduleVersion}`;


// ------  POST ------
export const scanFileSystemUrl = `${getAvailableUrl}/scanFileSystem`;


// ------  PUT ------
export const addAvailableModuleUrl = (moduleName: ETCSModuleNames, moduleVersion: string, pathToJar: string) =>
    `${getAvailableUrl}/${moduleName}/${moduleVersion}?pathToJar=${pathToJar}`;
export const changeConfigValueUrl = (moduleName: ETCSModuleNames, moduleVersion: string, variableName: string, newValue: string) =>
    `${getAvailableUrl}/${moduleName}/${moduleVersion}/configValues?variableName=${variableName}&newValue=${newValue}`;
export const setCommandLineArgumentsUrl = (moduleName: ETCSModuleNames, moduleVersion: string) =>
    `${moduleManagerUrl}/${moduleName}/${moduleVersion}/commandLineArguments`;


// ------  DELETE ------
export const deleteAvailableModuleUrl = (moduleName: ETCSModuleNames, moduleVersion: string) =>
    `${getAvailableUrl}/${moduleName}/${moduleVersion}`;
export const deleteCommandLineArgumentsUrl = (moduleName: ETCSModuleNames, moduleVersion: string) =>
    `${moduleManagerUrl}/${moduleName}/${moduleVersion}/commandLineArguments`;