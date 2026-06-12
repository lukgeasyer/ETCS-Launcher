## Developing

Use the IDE of youre choice, WebStorm or Visual Studio Code is recommended. The dependencies for the
frontend are organized via `npm` and recorded in the `package.json` folder.

Installing the required dependencies is done via executing `npm install`.

**Note:** This command has to be executed in the directory where the `package.json` file is located, so
in the `etcsLauncher/frontend` folder in this case.

**Note:** For developing under Windows you need to have Python and Visual Studio with the newest
packages for desktop development with C++ installed.

Once you've installed dependencies you can start a
development server. This server serves your HTML pages as a website or executes the
`main.cjs` file within the `dist` folder which starts the Electron application (without the backend).

1. For developing as a web-application run

```bash
npm run dev:svelte
```

**Note:**  This is running in a browser, thus `NodeJS` modules are not available.
These modules include support for operating in the file system of the user, for example.

2. For developing as a desktop-application using Electron run

```bash
npm run dev:all
```

## Preparation for Build

### 1. Building the backend `.jar`

Ensure that there is a `.jar` of the backend-version you want to include
within the production build inside the `backend/target` folder.

See [README of backend](../backend/README.md) for information about
building the `.jar`.

### 2. Including a JDK for supported platforms

In the folder `etcsLauncher/jdk` are empty directories for the JDK's of
each supported platform. Please make sure to include the appropriate JDK's
within these directories. You can download the JDK's from a vendor like
[Bellsoft](https://bell-sw.com/pages/downloads/).

**Note:** The ETCS-Graph module requires JavaFX. The bundled JDK thus needs to include JavaFX.

**Note:** Ensure that the content of each JDK gets copied into the directory.
The folder structures for the JDKs are

### 1. macOS on M-chips

`etcsLauncher/jdk/mac/jdk-21.0.4-arm64/[bin, conf, include, jmods, legal, ...]/...`

### 2. macOS on Intel chips

`etcsLauncher/jdk/mac/jdk-21.0.4-x64/[bin, conf, include, jmods, legal, ...]/...`

### 3. Windows on Intel chips

`etcsLauncher/jdk/win/jdk-21.0.4-x64/[bin, conf, include, jmods, legal, ...]/...`

## Building

Building the Electron desktop-application involves several steps:

### 1. Building CommonJS files for Electron main script

As of writing this document, it is not possible to run a `main.ts` file as the main
script for an Electron application, only JavaScript (or CommonJavaScript) files are supported.
As this project is supposed to use TyeScript
there is a `main.ts` file that is supposed to be the main script for the Electron application.
However, as mentioned, Electron needs a `.js` or in this case `.cjs` (CommonJS) file to run.

Executing

```bash
npm run build:typescript
```

compiles `.js` file equivalents for the `main.ts` and `preload.ts` files and placing them inside
the `dist` directory. After this operation a bash command renames all `*.js` files in the `dist` directory
to `*.cjs` files and all `*.js.map` files to `.cjs.map` files.

### 2. Rendering the Svelte HTML pages

Executing

```bash
npm run build:svelte
```

renders the Svelte pages into HTML files that can be served to the Electron application, placing
them inside the `build` folder.
Creating the correct pages for the Electron application is done by using an
[Adapter](https://kit.svelte.dev/docs/adapters) within the `svelte.config.js` file.

### 3. Building the Electron desktop-application

Executing

````bash
npm run build:electron
````

builds the Electron desktop-application into a native executable for the platforms defined in `build.config.yml`.
It places the executables inside the ``out`` directory.

For more information see the [official documentation of electron-builder](https://www.electron.build).

We need to intercept the build process by copying the backend `.jar` into the
`Resources/backend` folder of our Electron desktop-application. Furthermore, we need to include an appropriate JDK
which is used for executing the backend `.jar` file and for executing the provided `.jar` files of the ETCS@EBD modules.
Thus, the user does not need to have Java installed.

This is done via the `extraResources` API in `build.config.yml`.

Once the building process has finished successfully the native executables and their respective installers
can be found inside the `out` folder.

**Note:** Electron-Builder does currently not support building the `.dmg` for macOS on Windows machines.
Run

````bash
npm run build:electron:windows
````

for building the `.exe` only.

## Testing

Tests are separated into unit-tests with [Vitest](https://vitest.dev) - found in `tests/vitest` - and
end-to-end tests with [Playwright](https://playwright.dev) - found in `tests/playwright`.

### Unit tests

Unit-tests check the utility functions found in `src/utils/backendUtils` and the stores found in
`src/stores`. They are executed by running

```bash
npm run test:unit
```

### End-To-End tests

End-to-end tests check the Electron application while simulating the backend responses. Running

```bash
npm run test:end2end
```

opens multiple instances of the Electron application and performs actions on the different pages.

Most tests are visual comparisons with `.png` pictures of the application. These
pictures are not included in git and thus have to be generated. They are automatically
generated when running the end-to-end tests for the first time. Every successive visual test uses
these `.png`'s as comparison. They are stored in `tests/playwright/end2end/routes/[...].ts-snapshots`.

**Note:** The end-to-end tests will always "fail" when running for the first time because there are no
screenshots to compare. This makes sense because the ETCS-Launcher will look different on different screen sizes so
including
screenshots in git is not feasible.


