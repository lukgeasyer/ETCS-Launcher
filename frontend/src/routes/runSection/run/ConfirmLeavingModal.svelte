<script lang="ts">
    import {getModalStore} from "@skeletonlabs/skeleton";

    // Props
    export let parent: any;

    const modalStore = getModalStore();

    function onConfirm(): void {
        if ($modalStore[0].response) {
            $modalStore[0].response(true);
        }
        modalStore.close();
    }
</script>

{#if $modalStore[0]}
    <div class="card p-5 w-modal shadow-xl space-y-4">
        <header class="text-2xl font-bold mb-4">Confirm Leaving</header>
        <span>There are still ETCS-Modules running. If you leave this page, they will be shut down.</span>

        <footer class="modal-footer {parent.regionFooter}">
            <button class="btn {parent.buttonNeutral}" on:click={parent.onClose}
            >{parent.buttonTextCancel}</button
            >
            <button class="btn {parent.buttonPositive}" on:click={onConfirm}>Leave and shut down running
                modules
            </button>
        </footer>
    </div>
{/if}