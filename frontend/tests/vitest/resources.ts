import type {ModuleInfos} from "../../src/global";
import {ETCSModuleNames} from "../../src/constants/ETCSModuleNames";

const mockedAvailableModules: ModuleInfos = {};
mockedAvailableModules["CORE1.3.4"] =
    {
        jarName: "core_1.3.4.jar",
        moduleName: ETCSModuleNames.CORE,
        version: "1.3.4",
        configDirectory: "test",
        commandLineArguments: [],
        configValues: []
    };
mockedAvailableModules["CORE1.3.5"] =
    {
        jarName: "core_1.3.5.jar",
        moduleName: ETCSModuleNames.CORE,
        version: "1.3.5",
        configDirectory: "test",
        commandLineArguments: [],
        configValues: []
    };
mockedAvailableModules["DMI1.4.0"] =
    {
        jarName: "dmi_1.4.0.jar",
        moduleName: ETCSModuleNames.DMI,
        version: "1.4.0",
        configDirectory: "test",
        commandLineArguments: [],
        configValues: []
    };

export default mockedAvailableModules;