import {type Writable, writable} from "svelte/store";
import type {ModuleInfos, ModulesList} from "../global";

export const availableModules: Writable<ModuleInfos> = writable({});

export const selectedModulesToRun: Writable<ModulesList> = writable({
    CORE: [],
    DMI: [],
    Graph: [],
    ScenarioControlServer: [],
    smartLogic: [],
    smartTMS: []
});

export const selectedModuleToCustomize = writable("none");

/**
 * Checks if the selected ETCS-Module to customize was deleted.
 * @param availableModules the available ETCS-Modules
 * @param selectedModuleToCustomize the selected ETCS-Module to customize
 */
function checkAvailabilityOfModuleToCustomize(availableModules: ModuleInfos, selectedModuleToCustomize: string) {
    let moduleToCustomizeIsAvailable = false;
    for (const moduleID in availableModules) {
        if (moduleID === selectedModuleToCustomize) {
            moduleToCustomizeIsAvailable = true;
        }
    }

    return !moduleToCustomizeIsAvailable ? "none" : selectedModuleToCustomize;
}

/**
 * Checks if one or many of the selected ETCS-Modules to run were deleted.
 * @param availableModules the available ETCS-Modules
 * @param selectedModulesToRun the selected ETCS-Modules to run
 */
function checkAvailabilityOfModulesToRun(availableModules: ModuleInfos, selectedModulesToRun: ModulesList) {
    const updatedSelectedModulesToRun: ModulesList = {
        CORE: [],
        DMI: [],
        Graph: [],
        ScenarioControlServer: [],
        smartLogic: [],
        smartTMS: []
    };

    Object.keys(availableModules).forEach(moduleID => {
        const moduleName = availableModules[moduleID]["moduleName"];
        const version = availableModules[moduleID]["version"];
        if (selectedModulesToRun[moduleName].find(moduleVersionSelected => moduleVersionSelected === version)) {
            updatedSelectedModulesToRun[moduleName].push(version);
        }
    });

    return updatedSelectedModulesToRun;
}

/**
 * Checks for deletion of selected modules every time the available ETCS-Modules have changed.
 */
availableModules.subscribe((updatedModules) => {
    selectedModuleToCustomize.update((selectedModuleToCustomize) =>
        checkAvailabilityOfModuleToCustomize(updatedModules, selectedModuleToCustomize)
    );

    selectedModulesToRun.update((selectedModulesToRun) =>
        checkAvailabilityOfModulesToRun(updatedModules, selectedModulesToRun)
    );
});
