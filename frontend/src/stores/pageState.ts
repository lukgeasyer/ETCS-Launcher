/**
 * Sets the state of the application, describing whether it could connect to the backend.
 * 0: All good
 * 1: Loading resources
 * 2: Error has occurred
 */

import {writable} from "svelte/store";

export const appState = writable(1);