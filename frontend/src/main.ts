// Electron
import windowStateManager from "electron-window-state";
import {app, BrowserWindow, dialog, ipcMain, Menu, nativeTheme, shell} from 'electron';
import contextMenu from "electron-context-menu";
import serve from 'electron-serve';

// Node
import path from 'path';
import {type ChildProcessWithoutNullStreams, spawn} from "child_process";

// Config Variables
const backendIP = 'localhost';
const backendPort = 8090;
const devIP = 'localhost';
const devPort = 5173;

// Location of the rendered html files (after executing "npm run build:svelte"
const serveURL = serve({directory: 'build'});

let backendRunning = false;
const dev = !app.isPackaged;
const darkModeActivated = nativeTheme.shouldUseDarkColors;
let backendProcess: ChildProcessWithoutNullStreams;
let mainWindow: Electron.BrowserWindow;

function createWindow() {
    const windowState = windowStateManager({
        defaultWidth: 1200,
        defaultHeight: 1000,
    });

    const mainWindow = new BrowserWindow({
        title: "ETCS-Launcher",
        backgroundColor: darkModeActivated ? '#15171f' : '#dfe0e2',
        width: 1200,
        height: 1000,
        minWidth: 800,
        minHeight: 800,
        center: true,
        webPreferences: {
            devTools: dev,
            nodeIntegration: true,
            spellcheck: false,
            preload: path.join(__dirname, 'preload.cjs'),
        },
    });

    contextMenu();

    windowState.manage(mainWindow);

    mainWindow.once('ready-to-show', () => {
        mainWindow.show();
        mainWindow.focus();
    });

    mainWindow.on('close', (event) => {
        event.preventDefault();
        windowState.saveState(mainWindow);
        app.quit();
    });
    return mainWindow;
}

function createMainWindow() {
    mainWindow = createWindow();

    if (dev) {
        loadVite(devPort);
    } else {
        serveURL(mainWindow);
    }
}

function loadVite(port: number) {
    mainWindow.loadURL(`http://${devIP}:${port}`).catch((e) => {
        console.log('Error loading URL, retrying', e);
        setTimeout(() => {
            loadVite(port);
        }, 200);
    });
}

function executeBackend() {
    const backendName = "etcslauncher.backend-1.1.0.jar";

        let javaLoc = path.join(process.resourcesPath, "backend", "jdk");
        switch (process.platform) {
            case "win32":
                javaLoc = path.join(javaLoc, "bin", "java.exe");
                break;
            case "darwin":
                javaLoc = path.join(javaLoc, "bin", "java");
                break;
            default:
                backendRunning = false;
        }
        const backendExecutablePath = path.join(process.resourcesPath, "backend", backendName);
        const jdkPathArgument = "--jdkPath=" + javaLoc;
        backendProcess = spawn(javaLoc, ["-jar", backendExecutablePath, jdkPathArgument], {cwd: app.getPath('userData')});

        backendProcess.on('error', () => {
            backendRunning = false;
            return;
        });

        backendProcess.on('exit', () => {
            backendRunning = false;
            return;
        });

        backendRunning = true;
}


async function shutdownBackend() {
    if (backendRunning) {
        await fetch(`http://${backendIP}:${backendPort}/processManager/stop`, {method: 'POST'});
    }
    fetch(`http://${backendIP}:${backendPort}/shutdown`, {method: 'POST'});
}

ipcMain.on('select-file', async () => {
    const result = await dialog.showOpenDialog(mainWindow, {
        properties: ['openFile'],
        filters: [
            {name: 'JAR Files', extensions: ['jar']}
        ]
    });

    if (!result.canceled && result.filePaths.length > 0) {
        const selectedFilePath = result.filePaths[0];
        mainWindow.webContents.send('file-selected', selectedFilePath.replace(/\\/g, "/"));
    }
});

ipcMain.on('open-config-folder', async (_event, configPath: string) => {
    configPath = path.join(...configPath.split('/'));
    shell.showItemInFolder(decodeURIComponent(configPath));
});

ipcMain.on('quit-app', () => {
    app.quit();
});

app.once('ready', () => {
    if (!dev) {
        executeBackend();
    }
    Menu.setApplicationMenu(null);
    createMainWindow();
});
app.on('activate', () => {
    if (!mainWindow) {
        createMainWindow();
    }
});

app.on('before-quit', async (event) => {
    event.preventDefault();
    if (!dev) {
        await shutdownBackend();
    }
    app.exit();
});