<script lang="ts">
    // Components
    import {Accordion, AccordionItem, ListBox, ListBoxItem, TabAnchor, TabGroup} from '@skeletonlabs/skeleton';

    // Enums
    import {ETCSModuleNames} from '../../../constants/ETCSModuleNames';

    // Icons
    import ModuleIcon from "$lib/icons/ModuleIcon.svelte";

    // Icons
    import RunModulesIcon from "$lib/icons/UI/RunModulesIcon.svelte";

    // Types
    import type {ModulesList} from "../../../global";

    // Stores
    import {availableModules, selectedModulesToRun} from "../../../stores/modulesStore";
    import {appState} from "../../../stores/pageState";
    import LoadingPage from "../../LoadingPage.svelte";
    import ErrorPage from "../../ErrorPage.svelte";

    // Svelte
    import {page} from "$app/stores";

    $: state = $appState;

    let availableModuleVersions: ModulesList;
    availableModules.subscribe(updatedModulesList => {
        availableModuleVersions = {
            CORE: [],
            DMI: [],
            Graph: [],
            ScenarioControlServer: [],
            smartLogic: [],
            smartTMS: []
        };

        Object.keys(updatedModulesList).forEach(moduleID => {
            const moduleName = updatedModulesList[moduleID]["moduleName"];
            const version = updatedModulesList[moduleID]["version"];
            availableModuleVersions[moduleName].push(version);
        });
    });

    let selectedModules: ModulesList = {
        CORE: [],
        DMI: [],
        Graph: [],
        ScenarioControlServer: [],
        smartLogic: [],
        smartTMS: []
    };
    selectedModulesToRun.subscribe(updatedSelection => {
        selectedModules = updatedSelection;
    });

    function handleModuleSelection(selectedModuleName: ETCSModuleNames, selectedVersion: string) {
        if (selectedModules[selectedModuleName].length > 0) {
            if (selectedModules[selectedModuleName].at(0) === selectedVersion) {
                selectedModules[selectedModuleName] = [];
            } else {
                selectedModules[selectedModuleName] = [selectedVersion];
            }
        }
    }
</script>

{#if state === 0}
    <TabGroup spacing="space-y-1" rounded="rounded-tl rounded-tr">
        <TabAnchor href="/runSection/select" selected={$page.url.pathname === '/runSection/select/'}>
            <span>Select</span>
        </TabAnchor>
        <TabAnchor href="/runSection/run" selected={$page.url.pathname === '/runSection/run/'}>Run</TabAnchor>

        <svelte:fragment slot="panel">
            <div class="flex">
                <section class="flex-1 card variant-ghost p-4">
                    <div class="flex justify-between items-center">
                        <h1 class="h3 mb-4">Available ETCS-Modules</h1>
                        <div class="flex space-x-2 mr-5">
                            <a
                                    href="/runSection/run"
                                    class="btn variant-filled-success mt-5 mb-5"
                            >
                                <span><RunModulesIcon/></span>
                                <span>Run Selected</span>
                            </a>
                        </div>
                    </div>
                    <div>
                        <Accordion>
                            {#each Object.values(ETCSModuleNames) as moduleName}
                                {#if availableModuleVersions[moduleName].length > 0}
                                    <AccordionItem open>
                                        <svelte:fragment slot="lead">
                                            <ModuleIcon {moduleName}/>
                                        </svelte:fragment>
                                        <svelte:fragment slot="summary">ETCS-{moduleName}</svelte:fragment>
                                        <svelte:fragment slot="content">
                                            <ListBox multiple>
                                                {#each availableModuleVersions[moduleName] as moduleVersion}
                                                    <ListBoxItem
                                                            active="variant-filled-success"
                                                            bind:group={selectedModules[moduleName]}
                                                            name="moduleVersion"
                                                            value={moduleVersion}
                                                            on:click={() => handleModuleSelection(moduleName, moduleVersion)}
                                                    >
                                                        Version {moduleVersion}
                                                    </ListBoxItem>
                                                {/each}
                                            </ListBox>
                                        </svelte:fragment>
                                    </AccordionItem>
                                {/if}
                            {/each}
                        </Accordion>
                    </div>
                </section>
            </div>
        </svelte:fragment>
    </TabGroup>
{:else if state === 1}
    <LoadingPage/>
{:else}
    <ErrorPage/>
{/if}