<script lang="ts">
    // Skeleton
    import type {ConicStop} from '@skeletonlabs/skeleton';
    import {ConicGradient, getModalStore} from '@skeletonlabs/skeleton';

    // Icons
    import SuccessIcon from "$lib/icons/UI/SuccessIcon.svelte";

    // Svelte
    import {onMount} from "svelte";

    // Utils
    import {stopRunningModules} from "../../../utils/backendUtils";

    // Stores
    import {appState} from "../../../stores/pageState";

    // Props
    export let parent: any;

    let waiting = true;

    const modalStore = getModalStore();
    const conicStops: ConicStop[] = [
        {color: 'transparent', start: 0, end: 25},
        {color: 'rgb(var(--color-primary-500))', start: 75, end: 100}
    ];

    onMount(async () => {
        try {
            await stopRunningModules();
            waiting = false;
        } catch (e) {
            appState.set(2);
        }

    })

    function onConfirm(): void {
        if ($modalStore[0].response) {
            $modalStore[0].response(true);
        }
        modalStore.close();
    }

</script>

{#if $modalStore[0]}
    {#if waiting}
        <div class="card p-5 w-modal shadow-xl space-y-4">
            <header class="text-2xl font-bold mb-4">Terminating Modules</header>
            <span>Waiting for termination of running modules... </span>
            <footer class="modal-footer {parent.regionFooter}">
                <div class="btn {parent.buttonPositive}">
                    <ConicGradient width="w-6" stops={conicStops} spin></ConicGradient>
                </div>
            </footer>
        </div>
    {:else }
        <div class="card p-5 w-modal shadow-xl space-y-4">
            <header class="text-2xl font-bold mb-4">Terminating Modules</header>
            <span>Waiting for termination of running modules... </span>
            <footer class="modal-footer {parent.regionFooter}">
                <button class="btn variant-filled-success" on:click={onConfirm}>
                    <span>Modules have terminated</span>
                    <SuccessIcon/>
                </button>
            </footer>
        </div>
    {/if}
{/if}