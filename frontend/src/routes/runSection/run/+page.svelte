<script lang="ts">
    // Enums
    import {ETCSModuleNames, stringToEnum} from '../../../constants/ETCSModuleNames';
    import type {ModuleInfo} from "../../../global";

    // Svelte
    import {onDestroy, onMount} from "svelte";
    import {page} from "$app/stores";
    import {beforeNavigate, goto} from "$app/navigation";

    // Skeleton
    import {
        getModalStore,
        type ModalComponent,
        type ModalSettings,
        Tab,
        TabAnchor,
        TabGroup
    } from '@skeletonlabs/skeleton';

    // Icons
    import ModuleIcon from "$lib/icons/ModuleIcon.svelte";
    import StopIcon from "$lib/icons/UI/StopIcon.svelte";
    import RunModulesIcon from "$lib/icons/UI/RunModulesIcon.svelte";

    // Utils
    import {AnsiUp} from 'ansi_up';
    import Stomp from "stompjs";
    import VirtualList from "svelte-tiny-virtual-list";
    import {getRunningModules, sendInput, startModules} from "../../../utils/backendUtils";
    import LoadingPage from "../../LoadingPage.svelte";
    import ErrorPage from "../../ErrorPage.svelte";

    // URLs
    import {
        runningModulesWebSocketURL,
        webSocketConnectionURL
    } from "../../../constants/backendURLs/processManagerURLs";

    // Modals
    import ConfirmLeavingModal from "./ConfirmLeavingModal.svelte";
    import WaitingForTerminationOfModulesModal from "./WaitingForTerminationOfModulesModal.svelte";

    // Stores
    import {selectedModulesToRun} from "../../../stores/modulesStore";
    import {appState} from "../../../stores/pageState";
    import type {NavigationTarget} from "@sveltejs/kit";

    // General fields
    $: state = $appState;
    $: modulesToRun = $selectedModulesToRun;
    const ansiUp = new AnsiUp();
    const modalStore = getModalStore();
    let runningModuleWebSocket: WebSocket;
    let allowedToLeave = true;

    // HTML related fields
    let inputField: HTMLInputElement;
    let moduleOutputContainer: Element;
    const moduleOutputContainerBorder = 1;
    let virtualListHeight = 0;
    let moduleOutputWidths: Record<ETCSModuleNames, number[]> = {
        CORE: [],
        DMI: [],
        Graph: [],
        ScenarioControlServer: [],
        smartLogic: [],
        smartTMS: []
    }

    // Modules related fields
    let selectedModule = "None";
    let runningModules = 0;
    let moduleInput = '';
    let moduleOutput: Record<ETCSModuleNames, string[]> = {
        CORE: [],
        DMI: [],
        Graph: [],
        ScenarioControlServer: [],
        smartLogic: [],
        smartTMS: []
    };

    /**
     * Checks for running modules when loading page and terminated them if present.
     * Then opens a WebSocket connection for each module selected to run.
     */
    onMount(async () => {
        if (Object.values(ETCSModuleNames).some(moduleName => {
            return modulesToRun[moduleName].length === 1;
        })) {
            appState.set(1);
            window.addEventListener('resize', updateVirtualListHeight);
            Object.values(ETCSModuleNames).forEach((moduleName) => {
                if (modulesToRun[moduleName].length === 1) {
                    if (selectedModule === "None") {
                        selectedModule = moduleName.toString();
                    }
                }
            });

            const numberOfRunningModules = await getNumberOfRunningModules();
            if (numberOfRunningModules != undefined) {
                runningModules = numberOfRunningModules;
                if (runningModules > 0) {
                    await stopModules();
                }
            } else {
                appState.set(2);
                return;
            }

            runningModuleWebSocket = new WebSocket(webSocketConnectionURL);
            runningModuleWebSocket.addEventListener("open", () => {
                appState.set(0);
            });
            runningModuleWebSocket.addEventListener("error", () => {
                appState.set(2);
            });

            const stompClient = Stomp.over(runningModuleWebSocket);
            stompClient.connect({}, () => {
                Object.values(ETCSModuleNames).forEach((moduleName) => {
                    if (modulesToRun[moduleName].length === 1) {
                        const subscribeUrl = runningModulesWebSocketURL(moduleName);
                        stompClient.subscribe(subscribeUrl, async (message: any) => {
                            if (message.body === "STARTED") {
                                runningModules += 1;
                            } else if (message.body === "STOPPED") {
                                runningModules -= 1;
                            } else {
                                if (!inputField.onfocus) {
                                    inputField.focus();
                                }
                                const parts = message.body.split(':');
                                const index = parseInt(parts[0], 10);
                                const moduleMessage = parts.slice(1).join(':').trim()
                                moduleOutput[moduleName][index] = moduleMessage;
                                moduleOutput[moduleName][index + 1] = "";
                                updateModuleOutputWidths(moduleMessage, index, moduleName);
                                updateModuleOutputWidths("", index + 1, moduleName);
                            }

                        });
                    }
                });
            });
        }
    });

    onDestroy(() => {
        if (runningModuleWebSocket) {
            runningModuleWebSocket.close();
        }
    });

    /**
     * Cancels the navigation if there are still modules running. Asks the user if they want to leave and shut down the
     * running modules.
     */
    beforeNavigate(async ({to, cancel}) => {
        if (state !== 2) {
            if (!allowedToLeave) {
                cancel();
                await askUserForConfirmationOnLeaving(to);
            } else {
                window.removeEventListener('resize', updateVirtualListHeight);
                runningModuleWebSocket.removeEventListener("open", () => {
                    appState.set(0);
                });
                runningModuleWebSocket.removeEventListener("error", () => {
                    appState.set(2);
                });
            }

        }
    });

    async function getNumberOfRunningModules() {
        try {
            const runningModulesResponse: Array<ModuleInfo> = await getRunningModules();
            return runningModulesResponse.length;
        } catch (e) {
            appState.set(2);
        }

    }

    async function startModulesToRun() {
        updateVirtualListHeight();
        moduleOutputWidths = {
            CORE: [],
            DMI: [],
            Graph: [],
            ScenarioControlServer: [],
            smartLogic: [],
            smartTMS: []
        };
        moduleOutput = {
            CORE: [],
            DMI: [],
            Graph: [],
            ScenarioControlServer: [],
            smartLogic: [],
            smartTMS: []
        };

        let modulesToRunMap: Map<ETCSModuleNames, string> = new Map<ETCSModuleNames, string>();
        Object.values(ETCSModuleNames).forEach(moduleName => {
            if (modulesToRun[moduleName].length === 1) {
                modulesToRunMap.set(moduleName, modulesToRun[moduleName].at(0) ?? "");
            }
        });

        try {
            await startModules(modulesToRunMap);
            allowedToLeave = false;
        } catch (e) {
            appState.set(2);
        }
    }

    /**
     * Displays a modal while waiting for the backend response to terminate all running modules.
     */
    async function stopModules() {
        await displayWaitingForTerminationModal();
        allowedToLeave = true;
        const numberOfRunningModules = await getNumberOfRunningModules();
        if (numberOfRunningModules != undefined) {
            runningModules = numberOfRunningModules;
        }
    }

    /**
     * Asks the user if they want to leave and shut down still running modules. If they confirm,
     * navigates to the URL they want to visit.
     * @param to the URL they want to visit
     */
    async function askUserForConfirmationOnLeaving(to: NavigationTarget | null) {
        const modalComponent: ModalComponent = {
            ref: ConfirmLeavingModal,
        };
        return new Promise<boolean>((resolve) => {
            const modal: ModalSettings = {
                type: 'component',
                component: modalComponent,
                response: async (r: boolean | undefined) => {
                    if (r) {
                        resolve(r);
                    } else {
                        resolve(false);
                    }
                }
            };
            modalStore.trigger(modal);
        }).then(async (confirmed: boolean) => {
            if (confirmed) {
                await stopModules();
                const url = to?.url.href;
                if (url) {
                    await goto(url);
                }
            }
        });
    }

    /**
     * Displays a modal while waiting for the backend response about the termination of running modules.
     */
    async function displayWaitingForTerminationModal() {
        const modalComponent: ModalComponent = {
            ref: WaitingForTerminationOfModulesModal,
            props: {
                runningModules: runningModules,

            },
        };
        return new Promise<boolean>((resolve) => {
            const modal: ModalSettings = {
                type: 'component',
                component: modalComponent,
                response: async (r) => {
                    resolve(r);
                }
            };
            modalStore.trigger(modal);
        }).then((r) => {
            return r;
        });
    }

    async function sendInputToRunningModule() {
        const moduleName = stringToEnum(selectedModule);
        try {
            await sendInput(moduleName, moduleInput);
            moduleInput = '';
        } catch (e) {
            appState.set(2);
        }

    }

    function handleKeyPress(event: KeyboardEvent & { currentTarget: (EventTarget & HTMLInputElement) }) {
        if (event.key === "Enter") {
            sendInputToRunningModule();
        }
    }

    function updateVirtualListHeight() {
        if (moduleOutputContainer) {
            // The class="pre ..." styled output window has a padding of 16 (defined by SkeletonUI package)
            virtualListHeight = moduleOutputContainer.getBoundingClientRect().height - 2 * 16 - 2 * moduleOutputContainerBorder;
            updateAllModuleOutputWidths();
        }
    }

    function updateAllModuleOutputWidths() {
        Object.values(ETCSModuleNames).forEach(moduleName => {
            moduleOutputWidths[moduleName] = moduleOutput[moduleName].map((line) => getDivHeight(line));
        });
    }

    /**
     * Computes the height of each message from a running module. This is done by creating a temporal div which has the same
     * properties as the div's in the virtual list with the message as its inner html. Its measured height is then the correct height
     * in the virtual list.
     * @param line the output line of a running module
     */
    function getDivHeight(line: string) {
        const testDivContainer = document.getElementById("virtualListContainer");
        const testDiv = document.createElement('div');
        testDiv.className = "virtual-list-wrapper";
        if (line === '') {
            testDiv.innerHTML = ansiUp.ansi_to_html(' ');
        } else {
            testDiv.innerHTML = ansiUp.ansi_to_html(line);
        }
        testDivContainer?.appendChild(testDiv);

        try {
            return testDiv.clientHeight;
        } finally {
            testDivContainer?.removeChild(testDiv);
        }
    }

    function updateModuleOutputWidths(line: string, index: number, moduleName: ETCSModuleNames) {
        moduleOutputWidths[moduleName][index] = getDivHeight(line);
    }
</script>


{#if state === 0}
    <body class="h-full flex flex-col">
    <TabGroup rounded="rounded-tl rounded-tr" spacing="space-y-1">
        <TabAnchor href="/runSection/select" selected={$page.url.pathname === '/runSection/select/'}>
            <span>Select</span>
        </TabAnchor>
        <TabAnchor href="/runSection/run" selected={$page.url.pathname === '/runSection/run/'}>Run</TabAnchor>
    </TabGroup>
    <div class="flex h-full flex-grow-0 flex-col overflow-hidden">
        {#if selectedModulesToRun && Object.values(ETCSModuleNames).some((moduleName) => {
            return (modulesToRun[moduleName].length === 1);
        })}
            <div class="flex">
                <div class="space-x-2 mr-5 mt-2">
                    <button class="btn btn-sm variant-filled-success pr-5"
                            on:click={startModulesToRun}
                            disabled={runningModules > 0 || state !== 0}>
                        <span><RunModulesIcon/></span>
                        <span>Start </span>
                    </button>
                    <button class="btn btn-sm variant-filled-error pr-5"
                            on:click={stopModules}
                            disabled={runningModules === 0 || state !== 0}>
                        <span><StopIcon/></span>
                        <span>Stop</span>
                    </button>
                </div>
                <div class="space-x-2 mr-5 text-sm mt-2">
                    <TabGroup active="variant-filled-primary" rounded="rounded-tl rounded-tr" spacing="space-y-3">
                        {#each Object.values(ETCSModuleNames) as moduleName}
                            {#if modulesToRun[moduleName] && modulesToRun[moduleName].length === 1}
                                <Tab bind:group={selectedModule} name="{moduleName}-tile" value={moduleName}>
                                    <div>
                                        <span class="m-1">{moduleName}</span>
                                    </div>
                                </Tab>
                            {/if}
                        {/each}
                    </TabGroup>
                </div>
            </div>

            {#each Object.values(ETCSModuleNames) as moduleName}
                {#if selectedModule === moduleName.toString()}
                    <div class="pre text-black dark:text-white text-sm flex-1 border-[{moduleOutputContainerBorder}px] overflow-auto border-surface-400-500-token my-2 bg-surface-50 dark:bg-black"
                         bind:this={moduleOutputContainer}>
                        {#if moduleOutput[moduleName].length === 0 && runningModules === 0}
                            <div class="text-b flex justify-center items-center h-full">
                                <p>Start Modules by pressing the Start button</p>
                            </div>
                        {:else}
                            <div id="virtualListContainer" class="list h-full">
                                <VirtualList
                                        width="100%"
                                        height={virtualListHeight}
                                        itemCount={moduleOutput[moduleName].length}
                                        itemSize={moduleOutputWidths[moduleName]}
                                        scrollToIndex={moduleOutput[moduleName].length === 0 || runningModules === 0 ? undefined : moduleOutput[moduleName].length - 1}
                                        scrollToAlignment='auto'>
                                    <div slot="item" let:index let:style {style}>
                                        {@html ansiUp.ansi_to_html(moduleOutput[moduleName][index])}
                                    </div>
                                </VirtualList>
                            </div>
                        {/if}
                    </div>
                    <div class="flex my-2">
                        <div class="input-group input-group-divider grid-cols-[auto_1fr_auto]">
                            <div class="input-group-shim">
                                <ModuleIcon {moduleName}/>
                            </div>
                            <input type="text"
                                   bind:this={inputField}
                                   placeholder="Input..."
                                   bind:value={moduleInput}
                                   on:keydown={handleKeyPress}
                                   disabled={runningModules === 0 || state !== 0}>
                            {#if moduleInput.length !== 0}
                                <button class="variant-filled-primary" disabled={moduleInput.length === 0}
                                        on:click={sendInputToRunningModule}>Send
                                </button>
                            {/if}
                        </div>
                    </div>
                {/if}
            {/each}
        {:else}
            <div class="pre text-black dark:text-white text-sm flex-1 border-[1px] overflow-auto border-surface-400-500-token my-2 bg-surface-50 dark:bg-black">
                <div class="flex justify-center items-center h-full">
                    <p>No Modules Selected</p>
                </div>
            </div>
        {/if}
    </div>
    </body>
{:else if state === 1}
    <body class="h-full flex flex-col">
    <TabGroup regionList="mb-2" rounded="rounded-tl rounded-tr" spacing="space-y-1">
        <TabAnchor href="/runSection/select" selected={$page.url.pathname === '/runSection/select/'}>
            <span>Select</span>
        </TabAnchor>
        <TabAnchor href="/runSection/run" selected={$page.url.pathname === '/runSection/run/'}>Run</TabAnchor>
    </TabGroup>
        <LoadingPage/>
    </body>
{:else}
    <ErrorPage/>
{/if}

