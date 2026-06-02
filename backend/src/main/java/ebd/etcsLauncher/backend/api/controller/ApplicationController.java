package ebd.etcsLauncher.backend.api.controller;

import ebd.etcsLauncher.backend.model.processManager.ProcessManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "app://-"})
public class ApplicationController {

    private final ProcessManager processManager;

    public ApplicationController(ProcessManager processManager) {this.processManager = processManager;}

    @PostMapping("/shutdown")
    public void shutdownBackend() {
        if(processManager.getRunningProcesses().isEmpty()) {
            System.exit(0);
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

}
