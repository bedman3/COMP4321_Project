package com.comp4321Project.searchEngine.Controller;

import com.comp4321Project.searchEngine.View.ServerStatusView;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServerStatusController {
    @CrossOrigin
    @GetMapping("/server-status")
    public ServerStatusView serverStatusController(@RequestParam(value = "status", defaultValue = "up") String status) {
        return new ServerStatusView(status);
    }
}
