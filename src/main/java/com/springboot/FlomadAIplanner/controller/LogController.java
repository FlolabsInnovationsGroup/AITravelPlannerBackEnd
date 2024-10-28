package com.springboot.FlomadAIplanner.controller;

import com.springboot.FlomadAIplanner.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LogController {

    @Autowired
    private LogService myService;

    @GetMapping("/perform")
    public String performAction() {
        myService.performAction();
        return "Action performed!";
    }
}
