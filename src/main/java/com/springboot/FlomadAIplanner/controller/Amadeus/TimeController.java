package com.springboot.FlomadAIplanner.controller.Amadeus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/api/amadeus")
public class TimeController {
    @CrossOrigin // allow all sources, otherwise will report CORs error
    @GetMapping("/time")
    public String getTime() {
        log.info("Retrieving time");
        RestTemplate restTemplate = new RestTemplate();
        String uri = "http://worldtimeapi.org/api/timezone/Europe/London";
        return restTemplate.getForObject(uri, String.class);
    }
}