<script lang="ts">
    // Components
    import {Accordion, AccordionItem} from '@skeletonlabs/skeleton';

    // Icons
    import RunModulesIcon from "$lib/icons/UI/RunModulesIcon.svelte";
    import CustomizeModulesIcon from "$lib/icons/UI/CustomizeModulesIcon.svelte";
    import ModuleIcon from "$lib/icons/ModuleIcon.svelte";

    // Enums
    import {ETCSModuleNames} from "../constants/ETCSModuleNames";
</script>

<div class="mb-4">
    <h1 class="h3 pb-3 mb-3 border-b border-surface-400-500-token">Version 1.1</h1>
    <h2 class="h5 mb-3">Features</h2>
    <Accordion>
        <AccordionItem>
            <svelte:fragment slot="lead">
                <RunModulesIcon/>
            </svelte:fragment>
            <svelte:fragment slot="summary">Run ETCS-Modules</svelte:fragment>
            <svelte:fragment slot="content"
            >Run one or multiple ETCS-Modules and view their output. Interact with running ETCS-Modules
                by sending input.
            </svelte:fragment
            >
        </AccordionItem>
        <AccordionItem>
            <svelte:fragment slot="lead">
                <CustomizeModulesIcon/>
            </svelte:fragment>
            <svelte:fragment slot="summary">Customize ETCS-Modules</svelte:fragment>
            <svelte:fragment slot="content"
            >Search and customize config values of ETCS-Modules and set their command line arguments.
            </svelte:fragment
            >
        </AccordionItem>
    </Accordion>
</div>
<div>
    <h1 class="h5 mb-3 pt-3 border-t border-surface-400-500-token">ETCS-Modules</h1>
    <Accordion>
        <AccordionItem>
            <svelte:fragment slot="lead">
                <ModuleIcon moduleName={ETCSModuleNames.CORE}/>
            </svelte:fragment>
            <svelte:fragment slot="summary">ETCS-CORE</svelte:fragment>
            <svelte:fragment slot="content"
            >Responsible for simulating the behaviour of a train driving on the track, following the ETCS-Rules.
            </svelte:fragment
            >
        </AccordionItem>
        <AccordionItem>
            <svelte:fragment slot="lead">
                <ModuleIcon moduleName={ETCSModuleNames.DMI}/>
            </svelte:fragment>
            <svelte:fragment slot="summary">ETCS-DMI</svelte:fragment>
            <svelte:fragment slot="content"
            >Responsible for displaying the Driver-Machine Interface.
            </svelte:fragment
            >
        </AccordionItem>
        <AccordionItem>
            <svelte:fragment slot="lead">
                <ModuleIcon moduleName={ETCSModuleNames.GRAPH}/>
            </svelte:fragment>
            <svelte:fragment slot="summary">ETCS-Graph</svelte:fragment>
            <svelte:fragment slot="content"
            >Tool to display parameters of simulated train as graphs, e.g. speed per distance or distance per time.
            </svelte:fragment
            >
        </AccordionItem>
        <AccordionItem>
            <svelte:fragment slot="lead">
                <ModuleIcon moduleName={ETCSModuleNames.SMART_TMS}/>
            </svelte:fragment>
            <svelte:fragment slot="summary">ETCS-smartTMS</svelte:fragment>
            <svelte:fragment slot="content"
            >Responsible for managing Movement Authorities.
            </svelte:fragment>
        </AccordionItem>
        <AccordionItem>
            <svelte:fragment slot="lead">
                <ModuleIcon moduleName={ETCSModuleNames.SMART_LOGIC}/>
            </svelte:fragment>
            <svelte:fragment slot="summary">ETCS-smartLogic</svelte:fragment>
            <svelte:fragment slot="content"
            >Responsible for approving or rejecting Movement Authorities requested by the smartTMS.
            </svelte:fragment
            >
        </AccordionItem>
        <AccordionItem>
            <svelte:fragment slot="lead">
                <ModuleIcon moduleName={ETCSModuleNames.SCENARIOCONTROLSERVER}/>
            </svelte:fragment>
            <svelte:fragment slot="summary">ETCS-ScenarioControlServer</svelte:fragment>
            <svelte:fragment slot="content"
            >Responsible for synchronizing running ETCS-Modules.
            </svelte:fragment
            >
        </AccordionItem>
    </Accordion>
</div>
