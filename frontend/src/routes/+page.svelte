<script lang="ts">
    // Components
    import {
        Accordion,
        AccordionItem,
        getModalStore,
        getToastStore,
        ListBox,
        ListBoxItem,
        type ModalComponent,
        type ModalSettings,
        Tab,
        TabGroup,
        type ToastSettings
    } from '@skeletonlabs/skeleton';
    import AddNewJarSection from './AddNewJarSection.svelte';
    import About from './AboutSection.svelte';
    import ConfirmDeletionModal from './ConfirmDeletionModal.svelte';

    // Icons
    import RunModulesIcon from "$lib/icons/UI/RunModulesIcon.svelte";
    import CustomizeModulesIcon from "$lib/icons/UI/CustomizeModulesIcon.svelte";
    import DeleteIcon from "$lib/icons/UI/DeleteIcon.svelte";
    import ModuleIcon from "$lib/icons/ModuleIcon.svelte";

    // Backend Utilities
    import {deleteAvailableModule} from '../utils/backendUtils';

    // Stores
    import {availableModules, selectedModulesToRun, selectedModuleToCustomize} from "../stores/modulesStore";

    // Enums
    import {ETCSModuleNames} from '../constants/ETCSModuleNames';

    // Types
    import type {ModulesList} from "../global";
    import {appState} from "../stores/pageState";
    import LoadingPage from "./LoadingPage.svelte";
    import ErrorPage from "./ErrorPage.svelte";

    const modalStore = getModalStore();
    const toastStore = getToastStore();

    $: state = $appState;

    let tabSet = "modulesTab";

    /**
     * Keeps track of the available ETCS-Modules. Updates whenever the object in the store has changed.
     */
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

    async function deleteSelectedModules() {
        const deletePromises: Promise<void>[] = [];
        Object.values(ETCSModuleNames).forEach((moduleName) => {
            if (selectedModules[moduleName].length > 0) {
                selectedModules[moduleName].forEach((moduleVersion) => {
                    const deletePromise = deleteAvailableModule(moduleName, moduleVersion)
                        .then(() => {
                            const confirmDeletionToast: ToastSettings = {
                                message: `Successfully deleted Version <strong>${moduleVersion}</strong> of <strong>ETCS-${moduleName}</strong>!`,
                                background: 'variant-filled-success',
                                autohide: true
                            };
                            toastStore.trigger(confirmDeletionToast);
                        })
                        .catch(() => {
                            const informOnErrorWhileDeletingToast: ToastSettings = {
                                message: `There was an error when deleting Version <strong>${moduleVersion}</strong> of ETCS-<strong>${moduleName}</strong>. Please try again.`,
                                background: 'variant-filled-error',
                                autohide: true
                            };
                            toastStore.trigger(informOnErrorWhileDeletingToast);
                        });

                    deletePromises.push(deletePromise);
                });
            }
        });
        await Promise.all(deletePromises);
    }

    async function confirmDeletion() {
        const modalComponent: ModalComponent = {
            ref: ConfirmDeletionModal,
            props: {
                modulesToDelete: selectedModules
            }
        };
        new Promise<boolean>((resolve) => {
            const modal: ModalSettings = {
                type: 'component',
                component: modalComponent,
                response: (r: boolean) => {
                    resolve(r);
                }
            };
            modalStore.trigger(modal);
        }).then(async (confirmed: boolean) => {
            if (confirmed) {
                await deleteSelectedModules();
                Object.values(ETCSModuleNames).forEach(moduleName => {
                    selectedModules[moduleName] = [];
                });
            }
        });
    }

    /**
     * Sets the selected ETCS-Modules to run in the store
     */
    function setSelectedModulesToRun() {
        selectedModulesToRun.set(selectedModules);
    }

    /**
     * Sets the selected ETCS-Module to customize in the store
     */
    function setSelectedModuleToCustomize() {
        let selectedModule = "none";
        Object.values(ETCSModuleNames).forEach(moduleName => {
            if (selectedModules[moduleName].length === 1) {
                selectedModule = moduleName.toString() + selectedModules[moduleName];
            }
        });

        selectedModuleToCustomize.set(selectedModule);
    }
</script>

{#if state === 0}
    <TabGroup rounded="rounded-tl rounded-tr" spacing="space-y-1">
        <Tab bind:group={tabSet} name="modulesTab" value={"modulesTab"}>
            <span>Modules</span>
        </Tab>
        <Tab bind:group={tabSet} name="aboutTab" value={"aboutTab"}>About</Tab>
        <svelte:fragment slot="panel">
            {#if tabSet === "modulesTab"}
                <div class="flex">
                    <section class="flex-1 card variant-ghost p-4 h-fit">
                        <div class="flex justify-between items-center">
                            <h1 class="h3 mb-4">Available ETCS-Modules</h1>
                            <div class="flex space-x-2 mr-5">
                                <a href="/runSection/run">
                                    <button
                                            name='runModulesButton'
                                            class="btn variant-filled-success mt-5 mb-5"
                                            on:click={setSelectedModulesToRun}
                                            disabled={!Object.values(ETCSModuleNames).some((moduleName) => {
                                        return selectedModules[moduleName].length > 0;
                                    }) ||
                                        Object.values(ETCSModuleNames).some((moduleName) => {
                                            return selectedModules[moduleName].length > 1;
                                        })}
                                    >
                                        <span><RunModulesIcon/></span>
                                    </button>
                                </a>
                                <a href="/customizeSection">
                                    <button
                                            name="customizeModuleButton"
                                            class="btn variant-filled-secondary mt-5 mb-5"
                                            on:click={setSelectedModuleToCustomize}
                                            disabled={!Object.values(ETCSModuleNames).some((moduleName) => {
                                        return selectedModules[moduleName].length > 0;
                                    }) ||
                                        Object.values(ETCSModuleNames).filter((moduleName) => {
                                            return selectedModules[moduleName].length > 0;
                                        }).length > 1 ||
                                        Object.values(ETCSModuleNames).some((moduleName) => {
                                            return selectedModules[moduleName].length > 1;
                                        })}
                                    >
                                        <span><CustomizeModulesIcon/></span>
                                    </button>
                                </a>
                                <button
                                        name="deleteModulesButton"
                                        on:click={confirmDeletion}
                                        class="btn variant-filled-error mt-5 mb-5"
                                        disabled={!Object.values(ETCSModuleNames).some((moduleName) => {
                                        return selectedModules[moduleName].length > 0;
                                    })}
                                >
                                    <span><DeleteIcon/></span>
                                </button>
                            </div>
                        </div>
                        <div>
                            <Accordion>
                                {#each Object.values(ETCSModuleNames) as moduleName}
                                    <AccordionItem open={availableModuleVersions[moduleName].length > 0}>
                                        <svelte:fragment slot="lead">
                                            <ModuleIcon {moduleName}/>
                                        </svelte:fragment>
                                        <svelte:fragment slot="summary">ETCS-{moduleName}</svelte:fragment>
                                        <svelte:fragment slot="content">
                                            {#if availableModuleVersions[moduleName].length === 0}
                                                <ListBox>
                                                    <p class="ml-4"><i>No Versions Available</i></p>
                                                </ListBox>
                                            {:else}
                                                <ListBox multiple>
                                                    {#each availableModuleVersions[moduleName] as moduleVersion}
                                                        <ListBoxItem
                                                                bind:group={selectedModules[moduleName]}
                                                                name="moduleVersion"
                                                                value={moduleVersion}
                                                        >
                                                            Version {moduleVersion}
                                                        </ListBoxItem>
                                                    {/each}
                                                </ListBox>
                                            {/if}
                                        </svelte:fragment>
                                    </AccordionItem>
                                {/each}
                            </Accordion>
                        </div>
                    </section>
                    <section class="flex-1 p-4 mx-2">
                        <AddNewJarSection addedVersions={availableModuleVersions}/>
                    </section>
                </div>
            {:else if tabSet === "aboutTab"}
                <About/>
            {/if}
        </svelte:fragment>
    </TabGroup>
{:else if state === 1}
    <LoadingPage/>
{:else}
    <ErrorPage/>
{/if}

