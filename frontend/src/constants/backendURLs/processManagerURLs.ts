import type {ETCSModuleNames} from "../ETCSModuleNames";
import {backendIP, backendPort} from "../configVariables";

export const processManagerUrl = `http://${backendIP}:${backendPort}/processManager`;

// ------  GET ------
export const getRunningModulesUrl = `${processManagerUrl}/running`;


// ------  POST ------
export const startModulesUrl = `${processManagerUrl}/run`;
export const sendInputUrl = (moduleName: ETCSModuleNames, input: string) => `${processManagerUrl}/running/${moduleName}?input=${input}`;
export const stopModulesUrl = `${processManagerUrl}/stop`;


// ------  PUT ------
export const setDelayUrl = (delay: string) => `${processManagerUrl}/delay&delayBetweenStarting=${delay}`;


// ------  WEBSOCKET ------
export const webSocketConnectionURL = `ws://${backendIP}:${backendPort}/processManager/webSocket`;
export const runningModulesWebSocketURL = (moduleName: ETCSModuleNames) => `/processManager/running/${moduleName}`;