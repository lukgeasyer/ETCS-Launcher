import {describe, expect, it} from "vitest";
import type {ModuleInfos, ModulesList} from "../../../src/global";
import {ETCSModuleNames} from "../../../src/constants/ETCSModuleNames";
import {availableModules, selectedModulesToRun, selectedModuleToCustomize} from "../../../src/stores/modulesStore";
import mockedAvailableModules from "../resources";

describe('Correct behaviour of checkAvailabilityOfModuleToCustomize function', () => {
    it('Sets selected module to customize to "none" if it was deleted', () => {
        availableModules.set(mockedAvailableModules);
        selectedModuleToCustomize.set("DMI1.4.0");
        selectedModuleToCustomize.update(selectedModule => {
            expect(selectedModule).toStrictEqual("DMI1.4.0");
            return selectedModule;
        });
        availableModules.update(() => {
            const newAvailableModules: ModuleInfos = {};
            newAvailableModules["CORE1.3.4"] =
                {
                    jarName: "core_1.3.4.jar",
                    moduleName: ETCSModuleNames.CORE,
                    version: "1.3.4",
                    configDirectory: "test",
                    commandLineArguments: [],
                    configValues: []
                };
            return newAvailableModules;
        });
        selectedModuleToCustomize.update(selectedModule => {
            expect(selectedModule).toStrictEqual("none");
            return selectedModule;
        })
    })
});

describe("Correct behaviour of checkDeletionOfModulesToRun function", () => {
    it("Removes selected module to run if it was deleted", () => {
        const mockedSelectedModulesToRun: ModulesList = {
            CORE: ["1.3.4"],
            DMI: ["1.4.0"],
            Graph: [],
            ScenarioControlServer: [],
            smartLogic: [],
            smartTMS: []
        };

        availableModules.set(mockedAvailableModules);
        selectedModulesToRun.set(mockedSelectedModulesToRun);
        selectedModulesToRun.update(selectedModules => {
            expect(selectedModules).toStrictEqual(mockedSelectedModulesToRun);
            return selectedModules;
        });
        availableModules.update(() => {
            const newAvailableModules: ModuleInfos = {};
            newAvailableModules["CORE1.3.4"] =
                {
                    jarName: "core_1.3.4.jar",
                    moduleName: ETCSModuleNames.CORE,
                    version: "1.3.4",
                    configDirectory: "test",
                    commandLineArguments: [],
                    configValues: []
                };
            return newAvailableModules;
        });
        selectedModulesToRun.update(selectedModules => {
            expect(selectedModules).toStrictEqual({
                CORE: ["1.3.4"],
                DMI: [],
                Graph: [],
                ScenarioControlServer: [],
                smartLogic: [],
                smartTMS: []
            });
            return selectedModules;
        });
    })
});