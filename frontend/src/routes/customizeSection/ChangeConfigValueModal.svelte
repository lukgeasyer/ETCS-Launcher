<script lang="ts">
    import {focusTrap, getModalStore} from '@skeletonlabs/skeleton';

    // Props
    /** Exposes parent props to this component. */
    export let parent: any;
    export let variableName: string;
    export let oldValue: string;
    let newValue = "";

    // Local
    const modalStore = getModalStore();

    // Handle Form Submission
    function onConfigChangeSubmit(): void {
        if ($modalStore[0].response) {
            $modalStore[0].response(newValue);
        }
        modalStore.close();
    }

    function handleKeydown(event: KeyboardEvent & { currentTarget: (EventTarget & HTMLInputElement) }) {
        if (event.key === "Enter") {
            onConfigChangeSubmit();
        }
    }
</script>

{#if $modalStore[0]}
    <div class="card p-5 w-modal shadow-xl space-y-4">
        <header class="text-2xl font-bold mb-4">Change Config Value</header>
        <form class="modal-form" use:focusTrap={true}>
            <label class="label mb-4">
                <span>Variable Name</span>
                <input class="input" type="text" placeholder={variableName} disabled/>
            </label>
            <form use:focusTrap={true}>
                <label class="label">
                    <span>New Value</span>
                    <input class="input" type="text" placeholder={oldValue} bind:value={newValue}
                           on:keydown={handleKeydown}/>
                </label>
            </form>
        </form>
        <footer class="modal-footer {parent.regionFooter}">
            <button class="btn {parent.buttonNeutral}" on:click={parent.onClose}>{parent.buttonTextCancel}</button>
            <button class="btn variant-filled-secondary {parent.buttonPositive}" on:click={onConfigChangeSubmit}
                    disabled={newValue.length === 0}>Change
                Value
            </button>
        </footer>
    </div>
{/if}
