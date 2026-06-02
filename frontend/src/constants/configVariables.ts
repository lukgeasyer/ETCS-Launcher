// IP where the backend opens REST endpoint
export const backendIP = 'localhost';

// Port where the backend opens REST endpoint
export const backendPort = 8090;

// Port where the Vite server serves the Svelte pages for development
export const devPort = 5173;

// Retry interval for connecting to the backend in [ms]
export const backendConnectRetryInterval = 500;

// Maximum amount of retries for connecting to the backend
export const maximumRetriesForBackendConnection = 20;

// Maximum waiting time for connecting to the backend in [ms] until displaying an error
export const maximumWaitingTimeForBackendResponse = backendConnectRetryInterval * maximumRetriesForBackendConnection