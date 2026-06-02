<script lang="ts">

    // Components
    import {
        Accordion,
        AccordionItem,
        getModalStore,
        ListBox,
        ListBoxItem,
        type ModalComponent,
        type ModalSettings,
        Table,
        tableMapperValues,
        type TableSource
    } from "@skeletonlabs/skeleton";
    import ChangeConfigValueModal from "./ChangeConfigValueModal.svelte";
    import LoadingPage from "../LoadingPage.svelte";
    import ErrorPage from "../ErrorPage.svelte";

    // Enums
    import {ETCSModuleNames, stringToEnum} from "../../constants/ETCSModuleNames.js";

    // Icons
    import ModuleIcon from "$lib/icons/ModuleIcon.svelte";
    import SearchIcon from "$lib/icons/UI/SearchIcon.svelte";

    // Backend Utils
    import {changeConfigValue} from "../../utils/backendUtils";

    // Types
    import type {ModuleInfos, ModulesList} from "../../global";

    // Stores
    import {availableModules, selectedModuleToCustomize} from "../../stores/modulesStore";
    import {appState} from "../../stores/pageState";

    const modalStore = getModalStore();

    $: state = $appState;

    let propertiesOfAvailableModules: ModuleInfos = {};
    let availableModuleVersions: ModulesList;
    let configValues: Record<string, Record<string, string>[]>;

    const configTables: Record<string, TableSource> = {};
    const configSearchKeys: Record<string, string> = {};
    const filteredConfigValues: Record<string, Record<string, string>[]> = {};

    availableModules.subscribe(updatedModulesList => {
        propertiesOfAvailableModules = updatedModulesList;
        availableModuleVersions = {
            CORE: [],
            DMI: [],
            Graph: [],
            ScenarioControlServer: [],
            smartLogic: [],
            smartTMS: []
        };

        configValues = {};

        Object.keys(updatedModulesList).forEach(moduleID => {
            configValues[moduleID] = updatedModulesList[moduleID]["configValues"];
            const moduleName = updatedModulesList[moduleID]["moduleName"];
            const version = updatedModulesList[moduleID]["version"];
            availableModuleVersions[moduleName].push(version);
        });
        fillConfigTables();
    });

    let selectedModuleName = "none";
    let selectedVersion = "none";
    selectedModuleToCustomize.subscribe(selectedModuleToCustomize => {
        if (selectedModuleToCustomize !== "none") {
            selectedModuleName = propertiesOfAvailableModules[selectedModuleToCustomize]["moduleName"];
            selectedVersion = propertiesOfAvailableModules[selectedModuleToCustomize]["version"];
        }
    });

    function fillConfigTables() {
        Object.keys(configValues).forEach(moduleID => {
            const moduleName = propertiesOfAvailableModules[moduleID]["moduleName"];
            const version = propertiesOfAvailableModules[moduleID]["version"];

            configTables[moduleName.toString() + version] = {
                head: ['Variable Name', 'Value'],
                body: tableMapperValues(configValues[moduleID], ['variableName', 'value']),
                meta: tableMapperValues(configValues[moduleID], ['variableName', 'value']),
            };

            if (configSearchKeys[moduleID] !== "") {
                const filterRegex = new RegExp(configSearchKeys[moduleID], "i");
                filteredConfigValues[moduleID] = [];
                Object.values(configValues[moduleID]).filter(record => filterRegex.test(record.variableName)).forEach(configEntry => filteredConfigValues[moduleID].push(configEntry));
                configTables[moduleID].body = tableMapperValues(filteredConfigValues[moduleID], ['variableName', 'value']);
                configTables[moduleID].meta = tableMapperValues(filteredConfigValues[moduleID], ['variableName', 'value']);
            }
        });
    }

    function handleModuleSelection(selectedModule: ETCSModuleNames, selectedModuleVersion: string) {
        if (selectedModule === selectedModuleName && selectedVersion === selectedModuleVersion) {
            selectedModuleName = "none";
            selectedVersion = "none";
            selectedModuleToCustomize.set("none");
        } else {
            selectedModuleName = selectedModule;
            selectedVersion = selectedModuleVersion;
            selectedModuleToCustomize.set(selectedModule.toString() + selectedModuleVersion);
        }
    }

    async function letUserSetNewConfigValue(configValueSelectionEvent: CustomEvent<string[]>) {
        const variableName = configValueSelectionEvent.detail[0];
        const oldValue = configValueSelectionEvent.detail[1];
        const modalComponent: ModalComponent = {
            ref: ChangeConfigValueModal,
            props: {
                variableName: variableName,
                oldValue: oldValue
            }
        };
        new Promise<string>((resolve) => {
            const modal: ModalSettings = {
                type: 'component',
                component: modalComponent,
                response: (r: string) => {
                    resolve(r);
                }
            };
            modalStore.trigger(modal);
        }).then(async (newValue: string) => {
            if (newValue) {
                const selectedModule = stringToEnum(selectedModuleName);
                try {
                    await changeConfigValue(selectedModule, selectedVersion, variableName, newValue);
                } catch (e) {
                    appState.set(2);
                }

            }
        });
    }

    function handleConfigFilter(configFilterChangeEvent: any) {
        const configFilter = configFilterChangeEvent.target.value;
        const moduleID = selectedModuleName.toString() + selectedVersion;
        if (configFilter === "") {
            configTables[moduleID].body = tableMapperValues(configValues[moduleID], ['variableName', 'value']);
            configTables[moduleID].meta = tableMapperValues(configValues[moduleID], ['variableName', 'value']);
        } else {
            const filterRegex = new RegExp(configFilter, "i");
            filteredConfigValues[moduleID] = [];
            Object.values(configValues[moduleID]).filter(record => filterRegex.test(record.variableName)).forEach(configEntry => filteredConfigValues[moduleID].push(configEntry));
            configTables[moduleID].body = tableMapperValues(filteredConfigValues[moduleID], ['variableName', 'value']);
            configTables[moduleID].meta = tableMapperValues(filteredConfigValues[moduleID], ['variableName', 'value']);
        }
    }

    function openConfigDirectory() {
        const configPathWithoutFilePrefix = propertiesOfAvailableModules[selectedModuleName.toString() + selectedVersion]["configDirectory"].replace('file://', '');
        window.electron.send('open-config-folder', configPathWithoutFilePrefix)
    }
</script>

{#if state === 0}
    <div class="flex h-full max-h-full">
        <section class="flex-1 h-fit max-w-sm card variant-ghost p-4">
            <div class="flex justify-between items-center">
                <h1 class="h3 mb-4">Available ETCS-Modules</h1>
            </div>
            <div>
                <Accordion>
                    {#each Object.values(ETCSModuleNames) as moduleName}
                        {#if availableModuleVersions[moduleName].length > 0}
                            <AccordionItem open={availableModuleVersions[moduleName].length > 0}>
                                <svelte:fragment slot="lead">
                                    <ModuleIcon {moduleName}/>
                                </svelte:fragment>
                                <svelte:fragment slot="summary">ETCS-{moduleName}</svelte:fragment>
                                <svelte:fragment slot="content">
                                    <ListBox>
                                        {#each availableModuleVersions[moduleName] as moduleVersion}
                                            {#if selectedModuleName === moduleName && selectedVersion === moduleVersion}
                                                <ListBoxItem
                                                        class="variant-filled-secondary"
                                                        bind:group={selectedVersion}
                                                        name="moduleVersion"
                                                        value={moduleVersion}
                                                        on:click={() => handleModuleSelection(moduleName, moduleVersion)}
                                                >
                                                    Version {moduleVersion}
                                                </ListBoxItem>
                                            {:else }
                                                <ListBoxItem

                                                        bind:group={selectedVersion}
                                                        name="moduleVersion"
                                                        value={moduleVersion}
                                                        on:click={() => handleModuleSelection(moduleName, moduleVersion)}
                                                >
                                                    Version {moduleVersion}
                                                </ListBoxItem>
                                            {/if}
                                        {/each}
                                    </ListBox>
                                </svelte:fragment>
                            </AccordionItem>
                        {/if}
                    {/each}
                </Accordion>
            </div>
        </section>
        <div class="flex-1 max-h-full h-full max-w-screen-2xl overflow-y-auto">
            {#if selectedModuleName !== "none" && selectedVersion !== "none"}
                <section class="card variant-ghost p-4 mx-4 max-h-full min-h-full flex flex-col">
                    <div class="flex justify-between items-center mb-4">
                        <h1 class="flex-1 h3">Config Values</h1>
                        <div class="flex-1 input-group input-group-divider grid-cols-[auto_1fr_auto]">
                            <div class="input-group-shim">
                                <SearchIcon/>
                            </div>
                            <input type="search" placeholder="Search..."
                                   bind:value={configSearchKeys[selectedModuleName.toString() + selectedVersion]}
                                   on:input={handleConfigFilter}
                            />
                        </div>
                    </div>
                    <div class="max-h-full h-full max-w-screen-2xl overflow-y-auto pr-2 mb-4">
                        <Table class="table-compact border-surface-400-500-token text-sm break-words"
                               on:selected={letUserSetNewConfigValue}
                               source={configTables[selectedModuleName.toString() + selectedVersion]}
                               interactive={true}/>
                    </div>
                    <div class="flex flex-col">
                        <h1 class="h3 mb-4">Properties</h1>
                        <span class="mb-2">Config Folder</span>

                        <button class="btn flex variant-ghost mb-4"
                                on:click={openConfigDirectory}>
                            <span class="break-all whitespace-normal">
                                {decodeURIComponent(propertiesOfAvailableModules[selectedModuleName.toString() + selectedVersion]["configDirectory"])}
                            </span>
                        </button>
                    </div>

                </section>
            {:else}
                <section class="card h-full variant-ghost p-4 mx-4">
                    <div class="flex justify-center items-center h-full">
                        <p>No Module Selected</p>
                    </div>
                </section>
            {/if}
        </div>
    </div>
{:else if state === 1}
    <LoadingPage/>
{:else}
    <ErrorPage/>
{/if}