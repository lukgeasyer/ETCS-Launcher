// Vitest
import {describe, expect, it} from 'vitest';

// Types
import type {ModuleInfos} from "../../../src/global";

// Enums
import {ETCSModuleNames} from "../../../src/constants/ETCSModuleNames";

// Mocks
import {getAvailableModulesAPIMock, getBadRequestServerMock} from "../../backendMock";
import mockedAvailableModules from "../resources";

// Backend Utils
import {
    addAvailableModule,
    changeConfigValue,
    deleteAvailableModule,
    getAvailableModules,
    sendInput,
    startModules,
    stopRunningModules
} from "../../../src/utils/backendUtils";

// Stores
import {availableModules} from "../../../src/stores/modulesStore";

// URLs
import {
    addAvailableModuleUrl,
    changeConfigValueUrl,
    deleteAvailableModuleUrl,
    getAvailableUrl
} from "../../../src/constants/backendURLs/moduleManagerURLs";
import {sendInputUrl, startModulesUrl, stopModulesUrl} from "../../../src/constants/backendURLs/processManagerURLs";


const errorCode = 400;

describe('Correct behaviour of getAvailableModules function', () => {

    it('Writes fetched modules correctly to availableModules - modules available', async () => {
        const getAvailableModulesMock = getAvailableModulesAPIMock(mockedAvailableModules);
        getAvailableModulesMock.listen();
        await getAvailableModules();
        availableModules.update(updatedAvailableModules => {
            expect(updatedAvailableModules).toStrictEqual(mockedAvailableModules);
            return updatedAvailableModules;
        });
        getAvailableModulesMock.close();
    });

    it('Writes fetched modules correctly to availableModules - no modules available', async () => {
        const emptyModules: ModuleInfos = {};
        const getAvailableModulesMock = getAvailableModulesAPIMock(emptyModules);
        getAvailableModulesMock.listen();
        await getAvailableModules();
        availableModules.update(updatedAvailableModules => {
            expect(updatedAvailableModules).toStrictEqual(emptyModules);
            return updatedAvailableModules;
        });
        getAvailableModulesMock.close();
    });

    it('Overwrites fetched modules correctly to availableModules', async () => {
        availableModules.set(mockedAvailableModules);
        const emptyModules: ModuleInfos = {};
        const getAvailableModulesMock = getAvailableModulesAPIMock(emptyModules);
        getAvailableModulesMock.listen();
        await getAvailableModules();
        availableModules.update(updatedAvailableModules => {
            expect(updatedAvailableModules).toStrictEqual(emptyModules);
            return updatedAvailableModules;
        });
        getAvailableModulesMock.close();
    });


    it('Throws an error if the request was bad', async () => {
        const badRequestMock = getBadRequestServerMock(getAvailableUrl, "GET", errorCode);
        badRequestMock.listen();
        await expect(() => getAvailableModules()).rejects.toThrowError(`${errorCode} Bad Request`);
        badRequestMock.close();
    });
});

describe("Correct behaviour of addAvailableModule function", () => {
    it('Throws an error if the request was bad', async () => {
        const badRequestMock = getBadRequestServerMock(addAvailableModuleUrl(ETCSModuleNames.CORE, "1.3.4", "core.jar"), "PUT", errorCode);
        badRequestMock.listen();
        await expect(() => addAvailableModule(ETCSModuleNames.CORE, "1.3.4", "core.jar")).rejects.toThrowError(`${errorCode} Bad Request`);
        badRequestMock.close();
    })
});

describe("Correct behaviour of deleteAvailableModule function", () => {
    it('Throws an error if the request was bad', async () => {
        const badRequestMock = getBadRequestServerMock(deleteAvailableModuleUrl(ETCSModuleNames.CORE, "1.3.4"), "DELETE", errorCode);
        badRequestMock.listen();
        await expect(() => deleteAvailableModule(ETCSModuleNames.CORE, "1.3.4")).rejects.toThrowError(`${errorCode} Bad Request`);
        badRequestMock.close();
    })
});

describe("Correct behaviour of startModules function", () => {
    it('Throws an error if the request was bad', async () => {
        const badRequestMock = getBadRequestServerMock(startModulesUrl, "POST", errorCode);
        badRequestMock.listen();
        await expect(() => startModules(new Map)).rejects.toThrowError(`${errorCode} Bad Request`);
        badRequestMock.close();
    })
});

describe("Correct behaviour of stopRunningModules function", () => {
    it('Throws an error if the request was bad', async () => {
        const badRequestMock = getBadRequestServerMock(stopModulesUrl, "POST", errorCode);
        badRequestMock.listen();
        await expect(() => stopRunningModules()).rejects.toThrowError(`${errorCode} Bad Request`);
        badRequestMock.close();
    })
});

describe("Correct behaviour of sendInput function", () => {
    it('Throws an error if the request was bad', async () => {
        const badRequestMock = getBadRequestServerMock(sendInputUrl(ETCSModuleNames.CORE, "test"), "POST", errorCode);
        badRequestMock.listen();
        await expect(() => sendInput(ETCSModuleNames.CORE, "test")).rejects.toThrowError(`${errorCode} Bad Request`);
        badRequestMock.close();
    })
});

describe("Correct behaviour of changeConfigValue function", () => {
    it('Throws an error if the request was bad', async () => {
        const badRequestMock = getBadRequestServerMock(changeConfigValueUrl(ETCSModuleNames.CORE, "1.3.4", "IP", "test"), "PUT", errorCode);
        badRequestMock.listen();
        await expect(() => changeConfigValue(ETCSModuleNames.CORE, "1.3.4", "IP", "test")).rejects.toThrowError(`${errorCode} Bad Request`);
        badRequestMock.close();
    })
});
