import {_electron} from "playwright";
import {expect, test} from "@playwright/test";
import {
    deleteAvailableModuleUrl,
    getAvailableUrl,
    scanFileSystemUrl
} from "../../../../src/constants/backendURLs/moduleManagerURLs";
import {ETCSModuleNames} from "../../../../src/constants/ETCSModuleNames";
import {mockedAvailableModules} from "../resources";

test('Error page looks as expected', async () => {
    const electronApp = await _electron.launch({args: ['dist/main.cjs']});
    const window = await electronApp.firstWindow();
    await window.waitForSelector('text=An error has occurred');
    await expect(window).toHaveScreenshot('errorHomePage.png');
    await electronApp.close()
});

test('Home page looks as expected', async () => {
    const electronApp = await _electron.launch({args: ['dist/main.cjs']});
    await electronApp.context().route(scanFileSystemUrl, route => {
        route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify(mockedAvailableModules),
        });
    });
    const window = await electronApp.firstWindow();
    await window.waitForSelector('text=1.3.4');
    await expect(window).toHaveScreenshot('homePageInitial.png');
    await electronApp.close()
});

test('About page looks as expected', async () => {
    const electronApp = await _electron.launch({args: ['dist/main.cjs']});
    await electronApp.context().route(scanFileSystemUrl, route => {
        route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify(mockedAvailableModules),
        });
    });
    const window = await electronApp.firstWindow();
    await window.waitForSelector('text=1.3.4');
    await window.getByText('About').click();
    await expect(window).toHaveScreenshot('aboutPageInitial.png');
    await window.getByRole('button', {name: 'Run ETCS-Modules'}).click();
    await window.getByRole('button', {name: 'Customize ETCS-Modules'}).click();
    await window.getByRole('button', {name: 'ETCS-CORE'}).click();
    await window.getByRole('button', {name: 'ETCS-DMI'}).click();
    await window.getByRole('button', {name: 'ETCS-Graph'}).click();
    await window.getByRole('button', {name: 'ETCS-smartTMS'}).click();
    await window.getByRole('button', {name: 'ETCS-smartLogic'}).click();
    await window.getByRole('button', {name: 'ETCS-ScenarioControlServer'}).click();
    await expect(window).toHaveScreenshot('aboutPageExpanded.png');
    await electronApp.close()
});

test('Add new .jar section looks as expected', async () => {
    const electronApp = await _electron.launch({args: ['dist/main.cjs']});
    await electronApp.context().route(scanFileSystemUrl, route => {
        route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify(mockedAvailableModules),
        });
    });
    const window = await electronApp.firstWindow();
    await window.waitForSelector('text=1.3.4');
    await expect(window.getByRole('button', {name: 'Select .jar File'})).toBeDisabled();
    await window.getByLabel('No Module SelectedETCS-').selectOption('CORE');
    const versionForm = window.getByPlaceholder('1.2.3');
    await versionForm.click();
    await versionForm.fill('1.3.4');
    await expect(window.getByRole('button', {name: 'Select .jar File'})).toBeDisabled();
    await versionForm.click();
    await versionForm.fill('1.3.3');
    await expect(window.getByRole('button', {name: 'Select .jar File'})).toBeEnabled();
    await window.getByRole('button', {name: 'Select .jar File'}).click();
    await expect(window).toHaveScreenshot('addJarCorrect.png');
    await electronApp.close()
});

test('Play button works as expected'
    , async () => {
        const electronApp = await _electron.launch({args: ['dist/main.cjs']});
        await electronApp.context().route(scanFileSystemUrl, route => {
            route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify(mockedAvailableModules),
            });
        });
        const window = await electronApp.firstWindow();
        await window.waitForSelector('text=1.3.4');
        await expect(window.locator('button[name="runModulesButton"]')).toBeDisabled();
        await window.getByText('Version 1.3.4').click();
        await window.getByText('Version 1.4.0').click();
        await window.getByText('Version 1.3.26').click();
        await window.getByText('Version 1.3.62').click();
        await window.getByText('Version 1.0.0').click();
        await expect(window.locator('button[name="runModulesButton"]')).toBeEnabled();
        await expect(window).toHaveScreenshot('playButtonEnabled.png');
        await window.getByText('Version 1.3.5').click();
        await expect(window.locator('button[name="runModulesButton"]')).toBeDisabled();
        await electronApp.close()
    });

test('Customize button works as expected'
    , async () => {
        const electronApp = await _electron.launch({args: ['dist/main.cjs']});
        await electronApp.context().route(scanFileSystemUrl, route => {
            route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify(mockedAvailableModules),
            });
        });
        const window = await electronApp.firstWindow();
        await window.waitForSelector('text=1.3.4');
        await expect(window.locator('button[name="customizeModuleButton"]')).toBeDisabled();
        await window.getByText('Version 1.3.4').click();
        await expect(window.locator('button[name="customizeModuleButton"]')).toBeEnabled();
        await expect(window).toHaveScreenshot('customizeButtonEnabled.png');
        await window.getByText('Version 1.3.5').click();
        await expect(window.locator('button[name="customizeModuleButton"]')).toBeDisabled();
        await electronApp.close()
    });

test('Delete works as expected'
    , async () => {
        const electronApp = await _electron.launch({args: ['dist/main.cjs']});
        await electronApp.context().route(scanFileSystemUrl, route => {
            route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify(mockedAvailableModules),
            });
        });
        await electronApp.context().route(getAvailableUrl, route => {
            route.fulfill({
                status: 200,
                contentType: 'application/json',
                body: JSON.stringify(mockedAvailableModules),
            });
        });
        await electronApp.context().route(deleteAvailableModuleUrl(ETCSModuleNames.CORE, "1.3.4"), route => {
            route.fulfill({
                status: 204,
                contentType: 'application/json',
            });
        });
        const window = await electronApp.firstWindow();
        await window.waitForSelector('text=1.3.4');
        await expect(window.locator('button[name="deleteModulesButton"]')).toBeDisabled();
        await window.getByText('Version 1.3.4').click();
        await expect(window.locator('button[name="deleteModulesButton"]')).toBeEnabled();
        await window.getByText('Version 1.3.5').click();
        await expect(window.locator('button[name="deleteModulesButton"]')).toBeEnabled();
        await expect(window).toHaveScreenshot('deleteButtonEnabled.png');
        await window.getByText('Version 1.3.4').click();
        await window.locator('button[name="deleteModulesButton"]').click();
        await window.waitForSelector('text=Confirm');
        await expect(window).toHaveScreenshot('deleteConfirmation.png');
        await window.getByRole('button', {name: 'Delete'}).click();
        await window.waitForSelector('text=There was an error');
        await expect(window).toHaveScreenshot('deletionError.png');
        await window.getByText('Version 1.3.4').click();
        await window.locator('button[name="deleteModulesButton"]').click();
        await window.getByRole('button', {name: 'Delete'}).click();
        await window.waitForSelector('text=Successfully deleted');
        await expect(window).toHaveScreenshot('deletionSuccessfully.png');
        await electronApp.close()
    });

test('Quit section looks as expected', async () => {
    const electronApp = await _electron.launch({args: ['dist/main.cjs']});
    await electronApp.context().route(scanFileSystemUrl, route => {
        route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify(mockedAvailableModules),
        });
    });
    const window = await electronApp.firstWindow();
    await window.waitForSelector('text=1.3.4');
    await window.locator('a').filter({hasText: 'Quit'}).click();
    await expect(window).toHaveScreenshot('quitSection.png')
    await electronApp.close()
});
