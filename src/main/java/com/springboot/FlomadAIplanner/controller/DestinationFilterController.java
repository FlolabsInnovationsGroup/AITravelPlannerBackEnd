package com.springboot.FlomadAIplanner.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.FlomadAIplanner.service.LLMService;

import lombok.extern.slf4j.Slf4j;

////////////////////////////////////////////

/**
 * @class
 * @description This is a controller that 
 */
//////////////////////////////////////////
@Slf4j
@RestController
public class DestinationFilterController {
    /////////////////////////
    //todo: implement Gemini Vertex pro here

    @Autowired
    private LLMService llmService;

    public DestinationFilterController(LLMService llmService) { 
        this.llmService = llmService;
    }


    @GetMapping("/destination_filter")
    public String getDestination(@RequestParam String temperature) throws IOException {
        log.info("Retrieving temperature: {}", temperature);
        return llmService.searchDestination(temperature);
    }
}