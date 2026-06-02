<script lang="ts">
    // Svelte
    import {onMount} from 'svelte';
    import {fade} from 'svelte/transition';

    // Backend Utils
    import {addAvailableModule} from '../utils/backendUtils';

    // Icons
    import AlertIcon from "$lib/icons/UI/AlertIcon.svelte";
    import DismissIcon from "$lib/icons/UI/DismissIcon.svelte";
    import SuccessIcon from "$lib/icons/UI/SuccessIcon.svelte";
    import AddFileIcon from "$lib/icons/UI/AddFileIcon.svelte";

    // Enums
    import {ETCSModuleNames, stringToEnum} from '../constants/ETCSModuleNames';

    export let addedVersions: Record<string, string[]>;

    let showAlert = false;
    let showSuccess = false;

    function showErrorAlert() {
        showAlert = true;
    }

    function dismissErrorAlert() {
        showAlert = false;
    }

    let lastAddedModuleName = '';
    let lastAddedVersion = '';

    function showUploadSuccess(moduleName: string, moduleVersion: string) {
        lastAddedModuleName = moduleName;
        lastAddedVersion = moduleVersion;
        showSuccess = true;
    }

    function dismissUploadSuccess() {
        lastAddedModuleName = '';
        lastAddedVersion = '';
        showSuccess = false;
    }

    const versionRegex = /^[0-9]{1,2}\.[0-9]{1,2}\.[0-9]{1,2}$/;
    let specifiedVersion = '';

    let selectedModule = 'None';

    function handleModuleSelection(moduleSelectionEvent: Event & { currentTarget: (EventTarget & HTMLSelectElement) }) {
        selectedModule = moduleSelectionEvent.currentTarget.value;
    }

    onMount(() => {
        window.electron.receive('file-selected', async (filePath: string) => {
            try {
                dismissErrorAlert();
                dismissUploadSuccess();
                await addAvailableModule(stringToEnum(selectedModule), specifiedVersion, filePath);
                showUploadSuccess(selectedModule, specifiedVersion);
                selectedModule = 'None';
                specifiedVersion = '';
            } catch (error) {
                showErrorAlert();
            }
        });
    });

    function selectFile() {
        window.electron.send('select-file');
    }
</script>


<h1 class="h3 mb-4">Add ETCS-Module</h1>
<p class="mb-4 ml-1">Select Module:</p>
<label class="label mb-4">
    <select bind:value={selectedModule} class="select"
            on:change={handleModuleSelection}>
        <option value="None">No Module Selected</option>
        {#each Object.values(ETCSModuleNames) as moduleName}
            <option value={moduleName}>ETCS-{moduleName}</option>
        {/each}
    </select>
</label>
<p class="mb-4">Specify Version:</p>
<label class="label mb-4 flex flex-col space-y-1">
    <input bind:value={specifiedVersion} class="input" placeholder="1.2.3" type="text"/>
</label>
<button
        class="btn variant-filled mt-2 mb-5"
        disabled={selectedModule === 'None' ||
			!specifiedVersion.match(versionRegex) ||
			addedVersions[selectedModule].includes(specifiedVersion)}
        on:click={selectFile}
><span><AddFileIcon/></span>
    <span>Select .jar File</span>
</button>
{#if showAlert}
    <aside class="alert variant-ghost-error w-full" transition:fade|local={{ duration: 200 }}>
        <div>
            <AlertIcon/>
        </div>
        <div class="alert-message">
            <div class="flex justify-between items-center">
                <h3 class="h3">Invalid .jar</h3>
                <button type="button" class="btn-icon" on:click={dismissErrorAlert}
                >
                    <DismissIcon/>
                </button
                >
            </div>
            <p>
                Could not add .jar file. Please ensure that the selected file is a valid .jar-file for the <b
            >{selectedModule}</b
            >
                module.
            </p>
        </div>
    </aside>
{:else if showSuccess}
    <aside class="alert variant-ghost-success w-full" transition:fade|local={{ duration: 200 }}>
        <div>
            <SuccessIcon/>
        </div>
        <div class="alert-message">
            <div class="flex justify-between items-center">
                <h3 class="h3">Added Module</h3>
                <button type="button" class="btn-icon" on:click={dismissUploadSuccess}
                >
                    <DismissIcon/>
                </button
                >
            </div>
            <p>Added Version <b>{lastAddedVersion}</b> of Module <b>{lastAddedModuleName}</b></p>
        </div>
    </aside>
{/if}

