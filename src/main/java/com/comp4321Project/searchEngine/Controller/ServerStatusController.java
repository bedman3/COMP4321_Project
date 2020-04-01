package com.comp4321Project.searchEngine.Controller;

import com.comp4321Project.searchEngine.Model.ServerStatusModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServerStatusController {
    @GetMapping("/server-status")
    public ServerStatusModel serverStatusController(@RequestParam(value = "status", defaultValue = "up") String status) {
        return new ServerStatusModel(status);
    }
}
