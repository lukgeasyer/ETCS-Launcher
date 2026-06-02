package ebd.etcsLauncher.backend.api.controller;

import ebd.etcsLauncher.backend.model.etcsModule.ETCSModule;
import ebd.etcsLauncher.backend.model.moduleManager.ModuleManager;
import ebd.etcsLauncher.backend.model.processManager.ProcessManager;
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleNames;
import ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleSet;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static ebd.etcsLauncher.backend.utils.etcsModuleUtils.ETCSModuleNames.stringToModuleName;

/**
 * Defines a REST API for static requests about running {@link ETCSModule}s.
 *
 * @author Lukas Geyer
 */
@RestController
@CrossOrigin(origins = {"http://localhost:5173", "app://-"})
@RequestMapping("/processManager")
public class ProcessManagerController {

    private final ProcessManager processManager;
    private final ModuleManager  moduleManager;

    public ProcessManagerController(ProcessManager processManager,
                                    ModuleManager moduleManager) {
        this.processManager = processManager;
        this.moduleManager = moduleManager;
    }

    // ----- GET -----

    @GetMapping("/running")
    public List<ETCSModule> getRunningModules() {
        return processManager.getRunningProcesses();
    }


    // ----- POST -----

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/run")
    public void runModules(@RequestBody Map<String, String> moduleMap) {
        try {
            ETCSModuleSet modulesToRun = new ETCSModuleSet();
            for(Map.Entry<String, String> entry : moduleMap.entrySet()) {
                ETCSModuleNames.ModuleName moduleName = stringToModuleName(entry.getKey());
                String                     version    = entry.getValue();
                modulesToRun.add(moduleManager.getAvailableModule(moduleName, version));
            }
            if(!processManager.runModules(modulesToRun)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        } catch(IllegalArgumentException illegalArgumentException) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch(Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/running/{moduleName}")
    public void sendInput(@PathVariable String moduleName, @RequestParam String input) {
        try {
            if(!processManager.forwardInputToRunningProcess(stringToModuleName(moduleName), input)) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch(Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/stop")
    public void stopRunningProcesses() {
        if(!processManager.destroyAllModuleProcesses()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ----- PUT -----

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/delay")
    public void setDelayBetweenStarting(@RequestParam String delayBetweenStarting) {
        try {
            processManager.setDelayBetweenModuleStarting(Long.parseLong(delayBetweenStarting));
        } catch(NumberFormatException numberFormatException) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

}
