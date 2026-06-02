<script lang="ts">
    import {getModalStore} from '@skeletonlabs/skeleton';

    // Props
    /** Exposes parent props to this component. */
    export let parent: any;

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
        <header class="text-2xl font-bold mb-4">Confirm Quit</header>
        <span>Are you sure you want to quit?</span>

        <footer class="modal-footer {parent.regionFooter}">
            <button class="btn {parent.buttonNeutral}" on:click={parent.onClose}
            >{parent.buttonTextCancel}</button
            >
            <button class="btn variant-filled-error" on:click={onConfirm}>Quit ETCS-Launcher</button>
        </footer>
    </div>
{/if}
