<script lang="ts">
    import '../app.postcss';
    import {page} from '$app/stores';

    // Highlight JS
    import hljs from 'highlight.js';
    import 'highlight.js/styles/github-dark.css';
    // Components
    import {
        AppBar,
        AppRail,
        AppRailAnchor,
        AppShell,
        autoModeWatcher,
        getModalStore,
        initializeStores,
        Modal,
        type ModalComponent,
        type ModalSettings,
        storeHighlightJs,
        storePopup,
        Toast
    } from '@skeletonlabs/skeleton';
    import ConfirmQuitModal from "./ConfirmQuitModal.svelte";
    // Floating UI for Popups
    import {arrow, autoUpdate, computePosition, flip, offset, shift} from '@floating-ui/dom';
    // Icons
    import HomeIcon from "$lib/icons/UI/HomeIcon.svelte";
    import RunModulesIcon from "$lib/icons/UI/RunModulesIcon.svelte";
    import CustomizeModulesIcon from "$lib/icons/UI/CustomizeModulesIcon.svelte";
    import QuitIcon from "$lib/icons/UI/QuitIcon.svelte";

    storeHighlightJs.set(hljs);

    storePopup.set({computePosition, autoUpdate, flip, shift, offset, arrow});

    initializeStores();

    const modalStore = getModalStore();

    async function confirmQuitting() {
        const modalComponent: ModalComponent = {
            ref: ConfirmQuitModal
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
                window.electron.send('quit-app');
            }
        });
    }
</script>

<svelte:head>{@html `<script>${autoModeWatcher.toString()} autoModeWatcher();</script>`}</svelte:head>

<body class="flex h-screen w-screen">
<Modal transitions={false}/>
<Toast/>
<AppShell
        class="text-2xl"
        slotHeader="border-b-[0.5px] border-surface-400-500-token"
        slotPageContent="max-h-full mx-10 py-6 space-y-8 text-base overflow-y-auto"
        slotSidebarLeft="flex-none h-full border-r-[0.5px] border-surface-400-500-token"
>
    <svelte:fragment slot="header">
        <AppBar
                gridColumns="grid-cols-3"
                slotDefault="place-self-center"
                slotTrail="place-content-end"
        >
            <svelte:fragment slot="lead">
                <div class="text-xl uppercase text-center">
                    <span class="ml-[3px]"></span>
                </div>
            </svelte:fragment>
            <div class="justify-center items-center">
                <h1 class="text-center text-3xl">ETCS-Launcher</h1>
                <p class="text-center text-sm font-thin">ETCS@EBD</p>
            </div>
        </AppBar>
    </svelte:fragment>
    <svelte:fragment slot="sidebarLeft">
        <AppRail>
            <AppRailAnchor href="/" name="Home Tile" selected={$page.url.pathname === '/'}>
                <svelte:fragment slot="lead">
                    <HomeIcon/>
                </svelte:fragment>
                <span>Home</span>
            </AppRailAnchor>
            <AppRailAnchor
                    href="/runSection/select"
                    name="Run Modules Tile"
                    selected={$page.url.pathname === '/runSection/select/' || $page.url.pathname === '/runSection/run/'}
                    title="Select and run ETCS-Modules"
            >
                <svelte:fragment slot="lead">
                    <RunModulesIcon/>
                </svelte:fragment>
                <span>Run</span>
            </AppRailAnchor>
            <AppRailAnchor
                    href="/customizeSection"
                    name="Customize Modules Tile"
                    selected={$page.url.pathname === '/customizeSection/'}
                    title="Customize ETCS-Modules"
            >
                <svelte:fragment slot="lead">
                    <CustomizeModulesIcon/>
                </svelte:fragment>
                <span>Customize</span>
            </AppRailAnchor>
            <svelte:fragment slot="trail">
                <AppRailAnchor class="cursor-pointer" name="Quit Tile" on:click={confirmQuitting}>
                    <svelte:fragment slot="lead">
                        <QuitIcon/>
                    </svelte:fragment>
                    <span>Quit</span>
                </AppRailAnchor>
            </svelte:fragment>
        </AppRail>
    </svelte:fragment>
    <slot/>
</AppShell>
</body>
