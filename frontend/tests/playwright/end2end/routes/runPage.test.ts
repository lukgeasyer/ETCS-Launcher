import {_electron} from "playwright";
import {expect, test} from "@playwright/test";
import {getAvailableUrl, scanFileSystemUrl} from "../../../../src/constants/backendURLs/moduleManagerURLs";
import {mockedAvailableModules, runningModules} from "../resources";
import {getRunningModulesUrl, stopModulesUrl} from "../../../../src/constants/backendURLs/processManagerURLs";


test('Error page looks as expected', async () => {
    const electronApp = await _electron.launch({args: ['dist/main.cjs']});
    const window = await electronApp.firstWindow();
    await window.waitForSelector('text=An error has occurred');
    await window.getByRole('link', {name: 'Run'}).click();
    await window.waitForSelector('text=An error has occurred');
    await expect(window).toHaveScreenshot('errorRunPage.png');
    await electronApp.close()
});

test('Run page looks as expected', async () => {
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
    const window = await electronApp.firstWindow();
    await window.waitForSelector('text=1.3.4');
    await window.getByRole('link', {name: 'Run'}).click();
    await window.waitForSelector('text=Run Selected');
    await expect(window).toHaveScreenshot('runPageInitial.png');
    await electronApp.close()
});

test('Run button works as expected', async () => {
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
    const window = await electronApp.firstWindow();
    await window.waitForSelector('text=1.3.4');
    await window.getByRole('link', {name: 'Run'}).click();
    await window.waitForSelector('text=Run Selected');
    await window.getByText('Version 1.3.4').click();
    await expect(window).toHaveScreenshot('CORE134Selected.png');
    await window.getByText('Version 1.3.5').click();
    await expect(window).toHaveScreenshot('CORE135Selected.png');
    await window.getByText('Version 1.3.4').click();
    await window.getByText('Version 1.4.0').click();
    await window.getByText('Version 1.3.26').click();
    await window.getByText('Version 1.3.62').click();
    await window.getByText('Version 1.0.0').click();
    await expect(window).toHaveScreenshot('multipleSelected.png');
    await electronApp.close()
});

test('Run section looks as expected - no modules selected', async () => {
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
    const window = await electronApp.firstWindow();
    await window.waitForSelector('text=1.3.4');
    await window.getByRole('link', {name: 'Run'}).click();
    await window.waitForSelector('text=Run Selected');
    await window.getByTestId('tab-group').getByText('Run', {exact: true}).click();
    await expect(window).toHaveScreenshot('runSectionNoModulesSelected.png');
    await electronApp.close();
});

test('Run sections looks as expected - modules selected, error connecting to websocket', async () => {
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
    const window = await electronApp.firstWindow();
    await window.waitForSelector('text=1.3.4');
    await window.getByRole('link', {name: 'Run'}).click();
    await window.waitForSelector('text=Run Selected');
    await window.getByText('Version 1.3.4').click();
    await window.getByText('Version 1.4.0').click();
    await window.getByText('Version 1.6.1').click();
    await window.getByText('Version 1.3.26').click();
    await window.getByText('Version 1.3.62').click();
    await window.getByText('Version 1.0.0').click();
    await window.getByRole('link', {name: 'Run Selected'}).click();
    await window.waitForSelector('text=An error has occurred');
    await expect(window).toHaveScreenshot('runSectionModulesSelectedError.png');
    await electronApp.close();
});

test('Run section looks as expected - modules are running when entering', async () => {
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
    await electronApp.context().route(stopModulesUrl, route => {
        route.fulfill({
            status: 204,
            contentType: 'application/json',
        });
    });
    await electronApp.context().route(getRunningModulesUrl, route => {
        route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify(runningModules),
        });
    });

    const window = await electronApp.firstWindow();
    await window.waitForSelector('text=1.3.4');
    await window.getByText('Version 1.3.4').click();
    await window.locator('button[name="runModulesButton"]').click();
    await window.waitForSelector('text=Terminating');
    await expect(window).toHaveScreenshot('runSectionModalWaitingForTermination.png');
    await electronApp.context().route(getRunningModulesUrl, route => {
        route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify(runningModules),
        });
    });
    await window.getByRole('button', {name: 'Modules Have Terminated'}).click();
    await window.waitForSelector('text=An error has occurred');
    await expect(window).toHaveScreenshot('runSectionModulesSelectedError.png');
});

test('Run section looks as expected - modules are running when entering, backend error', async () => {
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
    await electronApp.context().route(stopModulesUrl, route => {
        route.fulfill({
            status: 500,
            contentType: 'application/json',
        });
    });
    const window = await electronApp.firstWindow();
    await window.waitForSelector('text=1.3.4');
    await window.getByText('Version 1.3.4').click();
    await window.locator('button[name="runModulesButton"]').click();
    await window.waitForSelector('text=An error has occurred');
    await expect(window).toHaveScreenshot('runSectionModulesSelectedError.png');
});