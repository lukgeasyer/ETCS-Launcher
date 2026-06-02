<script lang="ts">
    import {getModalStore} from '@skeletonlabs/skeleton';
    // Enums
    import {ETCSModuleNames} from '../constants/ETCSModuleNames';

    //Icons
    import ModuleIcon from "$lib/icons/ModuleIcon.svelte";

    // Props
    /** Exposes parent props to this component. */
    export let parent: any;
    export let modulesToDelete: Record<string, string[]>;

    // Local
    const modalStore = getModalStore();

    // Handle Form Submission
    function onConfirm(): void {
        if ($modalStore[0].response) {
            $modalStore[0].response(true);
        }
        modalStore.close();
    }
</script>

{#if $modalStore[0]}
    <div class="card p-5 w-modal shadow-xl space-y-4">
        <header class="text-2xl font-bold mb-4">Confirm Deletion</header>
        <span>Delete the following Module/s:</span>

        <dl class="list-dl">
            {#each Object.values(ETCSModuleNames) as moduleName}
                {#if modulesToDelete[moduleName] && modulesToDelete[moduleName].length > 0}
                    {#each modulesToDelete[moduleName] as moduleVersion}
                        <div>
                            <span class="badge"><ModuleIcon moduleName={moduleName}/></span>
                            <span class="flex-auto">
								<dt>ETCS-{moduleName}</dt>
								<dd>Version {moduleVersion}</dd>
							</span>
                        </div>
                    {/each}
                {/if}
            {/each}
        </dl>
        <footer class="modal-footer {parent.regionFooter}">
            <button class="btn {parent.buttonNeutral}" on:click={parent.onClose}
            >{parent.buttonTextCancel}</button
            >
            <button class="btn {parent.buttonPositive}" on:click={onConfirm}>Delete</button>
        </footer>
    </div>
{/if}
