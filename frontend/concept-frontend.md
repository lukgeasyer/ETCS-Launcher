# Konzeptdokumentation

## etcsLauncher - Frontend

> **Tools:** Svelte, TypeScript, Tailwind CSS, HTML, Electron
>
> **UI-Library:** [SkeletonUI](https://www.skeleton.dev)

Die grafische Oberfläche - also das Frontend - vom ETCS-Launcher sind Svelte-Components, die traditionell
eine Website darstellen, gepackt als Desktop-Anwendung über [Electron](https://www.electronjs.org/de/).

## Svelte Component

Eine Svelte-Component beinhaltet TypeScript Quellcode zum Steuern der dynamischen Inhalte der Seite,
HTML-Quellcode, der die Struktur der Seite festlegt und darin eingebettet Tailwind CSS Klassen
(hier beispielsweise ```class="card variant-ghost p-4 m-4"```), die die
HTML-Elemente "stylen":

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

Skeleton UI ist eine Library für UI-Elemente in Svelte. Diese UI-Elemente können zu Beginn
der TypeScript-Komponente importiert und als Komponente im HTML-Code verwendet werden
([Accordions](https://www.skeleton.dev/components/accordions) als Beispiel):

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

## Dateisystem

Das Dateisystem des Frontends folgt einem speziellen Aufbau:

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

Ordner der Form `__ordner__` sind Ordner, die nicht in git enthalten sind und erst im Laufe der
Entwicklungsphase automatisch erstellt werden.

Im Folgenden sollen alle oben gezeigten Ordner kurz beschrieben werden:

Ordner, die nicht in git enthalten sind:

- `.svelte-kit` beinhaltet die [Svelte-Kit](https://kit.svelte.dev) Anwendung
- `build` beinhaltet generierte HTML Dateien, die aus den `.svelte` Dateien erzeugt werden
- `dist` beinhaltet `.cjs` Dateien, die beim Kompilieren von TypeScript Dateien erzeugt werden, wenn beispielsweise
  keine TypeScript-Dateien von bestimmten Paketen unterstützt werden
- `node_modules` beinhaltet den Quellcode aller hinzugefügten Packages, also beispielsweise Electron,
  Svelte oder Tailwind CSS
- `out` beinhaltet die gebaute Desktop-Anwendung, die als Release bereitgestellt werden kann

Ordner, die in git enthalten sind:

- `src/constants` beinhaltet Konstanten und Konfigurationsvariablen
- `src/lib` beinhaltet global genutzte Resourcen, wie beispielsweise Backend URLs oder Icons
- `src/routes` beinhaltet die Svelte Components, die die Seiten der Anwendung definieren
- `src/stores` beinhalten Svelte writable Variablen, die global genutzt werden
- `src/utils` beinhaltet Utility-Funktionen, beispielsweise bei der Kommunikation mit dem Backend
- `src/global.d.ts` beinhaltet global genutzte Definitionen von TypeScript Typen
- `src/main.ts` beinhaltet die Definition der Electron Anwendung
- `src/preload.ts` beinhaltet vorgeladene Skripte, beispielsweise für die Kommunikation zwischen der
  Electron-Umgebung, die Zugriff auf das Dateisystem hat, und den Svelte Components
- `tests` beinhaltet Tests
- `package.json` beinhaltet die Definition der hinzugefügten Packages und Skripte

Für weitere Informationen zum Entwicklungsprozess siehe das `README.md`.