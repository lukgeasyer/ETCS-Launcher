/// <reference types="@sveltejs/kit" />
/// <reference types="svelte" />
/// <reference types="vite/client" />

import {ETCSModuleNames} from "./constants/ETCSModuleNames";

declare global {
    interface Window {
        electron: Electron
    }
}

declare type ModulesList = Record<ETCSModuleNames, string[]>;

declare type ModuleInfo = {
    jarName: string;
    moduleName: ETCSModuleNames;
    version: string;
    configDirectory: string,
    commandLineArguments: string[];
    configValues: Record<string, string>[];
};

declare type ModuleInfos = {
    [moduleID: string]: ModuleInfo;
};

