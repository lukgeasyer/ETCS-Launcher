# Concept documentation ETCS-Launcher - Frontend

> **Tools:** Svelte, TypeScript, Tailwind CSS, HTML, Electron
>
> **UI-Library:** [SkeletonUI](https://www.skeleton.dev)

The graphical UI of the ETCS-Launcher consists of Svelte-components which are served traditionally as a website bundled into a
desktop application via [Electron](https://www.electronjs.org/de/).

## Svelte Component

A Svelte-component consists of TypeScript to specify the dynamic contents of the HTML. Styling is done via Tailwind CSS (here for example the
```class="card variant-ghost p-4 m-4"``` part):

```
<------- Component.svelte -------> 

<-- TypeScript section -->

<script lang="ts">
    import foo from bar;
    
    let x: string;
    
    function foo() {...}
    
    ...
</script>

</-- TypeScript section -->

<-- HTML with Tailwind CSS section -->

<div class="card variant-ghost p-4 m-4"
    <h1>Welcome!</h1>
</div>

</-- HTML with Tailwind CSS section -->
```

## Skeleton UI

Skeleton UI is a style component library. The used elements can be imported at the beginning of the TypeScript part.
([Accordions](https://www.skeleton.dev/components/accordions) as an example):

```
<script lang="ts">
    import { Accordion, AccordionItem } from '@skeletonlabs/skeleton';
</script>

<Accordion>
	<AccordionItem open>
		<svelte:fragment slot="lead">(icon)</svelte:fragment>
		<svelte:fragment slot="summary">(summary)</svelte:fragment>
		<svelte:fragment slot="content">(content)</svelte:fragment>
	</AccordionItem>
	<AccordionItem>
		<svelte:fragment slot="lead">(icon)</svelte:fragment>
		<svelte:fragment slot="summary">(summary)</svelte:fragment>
		<svelte:fragment slot="content">(content)</svelte:fragment>
	</AccordionItem>
	<!-- ... -->
</Accordion>
```

## File system

The file system of the frontend follows a specific structure:

```
frontend
│    
└───__.svelte-kit__
│    
└───__build__
│    
└───__dist__
│    
└───__node_modules__
│    
└───__out__
│
└───src
│   │
│   └───constants
│   │  
│   └───lib
│   │
│   └───routes
│   │
│   └───stores
│   │
│   └───utils
│   │
│   │  global.d.ts
│   │  main.ts
│   │  preload.ts
│   │  [other files]
│ 
└───static
│    
└───tests
│
│   package.json
│   [other files]
``` 

Folders of the form `__folder__` are folders not included in git which get created during the development process.

Folders not included in git:

- `.svelte-kit` includes the [Svelte-Kit](https://kit.svelte.dev) executable
- `build` includes HTML files which get created from the `.svelte` files
- `dist` includes `.cjs` files which get compiled from TypeScript files in cases where TypeScript is not supported for certain packages
- `node_modules` includes the source code for the npm dependencies
- `out` includes the bundled application binaries

Folders included in git:

* `src/constants` contains constants and configuration variables.
* `src/lib` contains globally used resources, such as backend URLs or icons.
* `src/routes` contains the Svelte components that define the application's pages.
* `src/stores` contains Svelte writable stores that are used globally throughout the application.
* `src/utils` contains utility functions, for example for communication with the backend.
* `src/global.d.ts` contains globally used TypeScript type definitions.
* `src/main.ts` contains the definition and initialization of the Electron application.
* `src/preload.ts` contains preload scripts, for example to facilitate communication between the Electron environment, which has access to the file system, and the Svelte components.
* `tests` contains the test suite.
* `package.json` contains the definitions of the project's dependencies (packages) and scripts.

For further information about the development process, please refer to the `README.md`.
