package ebd.etcsLauncher.backend.api.controller;

import ebd.etcsLauncher.backend.model.etcsModule.ETCSModule;
import ebd.etcsLauncher.backend.model.moduleManager.ModuleManager;
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleVersion;
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.SortedETCSModuleArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeMap;

import static ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleNames.stringToModuleName;

/**
 * Defines a REST API for static requests about available {@link ETCSModule}s.
 *
 * @author Lukas Geyer
 */
@RestController
@CrossOrigin(origins = {"http://localhost:5173", "app://-"})
@RequestMapping("/moduleManager")
public class ModuleManagerController {

    private final ModuleManager moduleManager;

    public ModuleManagerController(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
        this.moduleManager.fillWithAvailableModules();
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    // ----- GET -----

    @GetMapping("/availableModules")
    public SortedETCSModuleArrayList getAvailableModules() {
        return moduleManager.getAvailableModules();
    }

    @GetMapping("/availableModules/{moduleName}/{moduleVersion}")
    public ETCSModule getAvailableModule(@PathVariable String moduleName,
                                         @PathVariable String moduleVersion) {
        try {
            return moduleManager.getAvailableModule(stringToModuleName(moduleName), moduleVersion);
        } catch(IllegalArgumentException illegalArgumentException) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/availableModules/{moduleName}/{moduleVersion}/configValues")
    public TreeMap<String, String> getConfigValues(@PathVariable String moduleName,
                                                   @PathVariable String moduleVersion) {
        try {
            return moduleManager.getAvailableModule(stringToModuleName(moduleName), moduleVersion).getConfigValues();
        } catch(IllegalArgumentException illegalArgumentException) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    // ----- POST -----

    @PostMapping("availableModules/scanFileSystem")
    public SortedETCSModuleArrayList scanForAvailableModules() {
        moduleManager.fillWithAvailableModules();

        return moduleManager.getAvailableModules();
    }


    // ----- PUT -----

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/availableModules/{moduleName}/{moduleVersion}")
    public ETCSModule addAvailableModule(@PathVariable String moduleName,
                                         @PathVariable String moduleVersion,
                                         @RequestParam String pathToJar) {
        ETCSModuleVersion etcsModuleVersion;
        try {
            etcsModuleVersion = new ETCSModuleVersion(moduleVersion);
        } catch(IllegalArgumentException illegalArgumentException) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        ETCSModule newModule = moduleManager.addModuleFromUserSystem(stringToModuleName(moduleName), etcsModuleVersion, Path.of(pathToJar));
        if(newModule == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return newModule;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/availableModules/{moduleName}/{moduleVersion}/configValues")
    public void changeConfigValue(@PathVariable String moduleName, @PathVariable String moduleVersion,
                                  @RequestParam String variableName, @RequestParam String newValue) {
        if(variableName == null || newValue == null || variableName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            if(!moduleManager.getAvailableModule(stringToModuleName(moduleName), moduleVersion)
                             .changeConfigVariable(variableName, newValue)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        } catch(IllegalArgumentException illegalArgumentException) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("availableModules/{moduleName}/{moduleVersion}/commandLineArguments")
    public void setCommandLineArguments(@RequestBody ArrayList<String> commandLineArguments,
                                        @PathVariable String moduleName,
                                        @PathVariable String moduleVersion) {
        try {
            moduleManager.getAvailableModule(stringToModuleName(moduleName), moduleVersion).setCommandLineArguments(commandLineArguments);
        } catch(IllegalArgumentException illegalArgumentException) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }

    // ----- DELETE -----

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/availableModules/{moduleName}/{moduleVersion}")
    public void removeAvailableModule(@PathVariable String moduleName,
                                      @PathVariable String moduleVersion) {
        try {
            ETCSModule moduleToRemove = moduleManager.getAvailableModule(stringToModuleName(moduleName), moduleVersion);
            if(!moduleManager.removeAvailableModule(moduleToRemove)) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch(IllegalArgumentException illegalArgumentException) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, illegalArgumentException.getMessage());
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/availableModules/{moduleName}/{moduleVersion}/commandLineArguments")
    public void deleteCommandLineArguments(@PathVariable String moduleName,
                                           @PathVariable String moduleVersion) {
        try {
            ArrayList<String> emptyList = new ArrayList<>();
            moduleManager.getAvailableModule(stringToModuleName(moduleName), moduleVersion).setCommandLineArguments(emptyList);
        } catch(IllegalArgumentException illegalArgumentException) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

}
