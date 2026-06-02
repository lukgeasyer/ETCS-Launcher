import {http, HttpResponse} from 'msw';
import {getAvailableUrl, scanFileSystemUrl} from "../src/constants/backendURLs/moduleManagerURLs";
import type {ModuleInfos} from "../src/global";
import type {HttpMethod} from "@sveltejs/kit";
import type {SetupServer} from "msw/node";
import {setupServer} from "msw/node";

export function getBadRequestServerMock(requestURL: string, requestType: HttpMethod, errorCode: number) {
    let worker: SetupServer;

    switch (requestType) {
        case "GET":
            worker = setupServer(http.get(requestURL, () => {
                return new HttpResponse(null, {
                    status: errorCode,
                })
            }));
            break;

        case "POST":
            worker = setupServer(http.post(requestURL, () => {
                return new HttpResponse(null, {
                    status: errorCode,
                })
            }));
            break;

        case "PUT":
            worker = setupServer(http.put(requestURL, () => {
                return new HttpResponse(null, {
                    status: errorCode,
                })
            }));
            break;

        case "DELETE":
            worker = setupServer(http.delete(requestURL, () => {
                return new HttpResponse(null, {
                    status: errorCode,
                })
            }));
            break;

        case "PATCH":
            worker = setupServer(http.patch(requestURL, () => {
                return new HttpResponse(null, {
                    status: errorCode,
                })
            }));
            break;

        default:
            throw Error("No such HTTP method: " + requestType);
    }

    return worker;
}

export function getScanFileSystemAPIMock(availableModules: ModuleInfos) {
    const body: any[] = [];
    Object.keys(availableModules).forEach(moduleID => {
        body.push({
            jarName: availableModules[moduleID]["jarName"],
            moduleName: availableModules[moduleID]["moduleName"],
            version: availableModules[moduleID]["version"],
            configDirectory: availableModules[moduleID]["configDirectory"],
            commandLineArguments: availableModules[moduleID]["commandLineArguments"],
            configValues: availableModules[moduleID]["configValues"]
        });
    });
    return setupServer(http.post(scanFileSystemUrl, () => {
        return new HttpResponse(JSON.stringify(body), {
            status: 200,
            headers: {
                'Content-Type': 'application/json',
            },
        })
    }));
}

export function getAvailableModulesAPIMock(availableModules: ModuleInfos) {
    const body: any[] = [];
    Object.keys(availableModules).forEach(moduleID => {
        body.push({
            jarName: availableModules[moduleID]["jarName"],
            moduleName: availableModules[moduleID]["moduleName"],
            version: availableModules[moduleID]["version"],
            configDirectory: availableModules[moduleID]["configDirectory"],
            commandLineArguments: availableModules[moduleID]["commandLineArguments"],
            configValues: availableModules[moduleID]["configValues"]
        });
    });

    return setupServer(http.get(getAvailableUrl, () => {
        return new HttpResponse(JSON.stringify(body), {
            status: 200,
            headers: {
                'Content-Type': 'application/json',
            },
        })
    }));
}