import {_electron} from "playwright";
import {expect, test} from "@playwright/test";
import {getAvailableUrl, scanFileSystemUrl} from "../../../../src/constants/backendURLs/moduleManagerURLs";
import {mockedAvailableModules} from "../resources";

test('Error page looks as expected', async () => {
    const electronApp = await _electron.launch({args: ['dist/main.cjs']});
    const window = await electronApp.firstWindow();
    await window.waitForSelector('text=An error has occurred');
    await window.getByRole('link', {name: 'Customize'}).click();
    await window.waitForSelector('text=An error has occurred');
    await expect(window).toHaveScreenshot('errorCustomizePage.png');
    await electronApp.close()
});

test('Customize page looks as expected', async () => {
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
    await window.getByRole('link', {name: 'Customize'}).click();
    await expect(window).toHaveScreenshot('customizePageInitial.png');
    await electronApp.close()
});

test('Selecting module looks as expected', async () => {
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
    await window.getByRole('link', {name: 'Customize'}).click();
    await window.getByText('Version 1.3.4').click();
    await expect(window).toHaveScreenshot('selectedCore134.png');
    await window.getByText('Version 1.3.5').click();
    await expect(window).toHaveScreenshot('selectedCore135.png');
    await window.getByText('Version 1.4.0').click();
    await expect(window).toHaveScreenshot('selectedDMI140.png');
    await window.getByText('Version 1.6.1').click();
    await expect(window).toHaveScreenshot('selectedGraph161.png');
    await window.getByText('Version 1.3.26').click();
    await expect(window).toHaveScreenshot('selectedtns1326.png');
    await window.getByText('Version 1.3.62').click();
    await expect(window).toHaveScreenshot('selectedsmartLogic1362.png');
    await window.getByText('Version 1.0.0').click();
    await expect(window).toHaveScreenshot('selectedSCS100.png');
    await electronApp.close()
});

test('Selecting variables looks as expected', async () => {
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
    await window.getByRole('link', {name: 'Customize'}).click();
    await window.getByText('Version 1.3.4').click();
    await window.getByRole('gridcell', {name: 'core1.3.4'}).click();
    await window.getByPlaceholder('value').fill('newvalue');
    await expect(window).toHaveScreenshot('changeConfigValue.png');
    await electronApp.close()
});

