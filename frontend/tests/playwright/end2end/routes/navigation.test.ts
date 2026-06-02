import {_electron} from "playwright";
import {expect, test} from "@playwright/test";
import {getAvailableUrl, scanFileSystemUrl} from "../../../../src/constants/backendURLs/moduleManagerURLs";
import {mockedAvailableModules} from "../resources";

test('Navigation works as expected', async () => {
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
    await window.getByText('Version 1.3.4').click();
    await window.locator('button[name="customizeModuleButton"]').click();
    await window.waitForSelector('text=Config Values');
    await expect(window).toHaveScreenshot('oneCustomizedSelected.png')
    await window.getByRole('link', {name: 'Home'}).click();
    await window.waitForSelector('text=About');
    await window.getByText('Version 1.3.4').click();
    await window.getByText('Version 1.4.0').click();
    await window.getByText('Version 1.3.26').click();
    await window.locator('button[name="runModulesButton"]').click();
    await window.waitForSelector('text=An error has occurred');
    await expect(window).toHaveScreenshot('runSectionModulesSelectedError.png')
    await electronApp.close()
});
