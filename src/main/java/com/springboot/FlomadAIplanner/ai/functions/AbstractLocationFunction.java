package com.springboot.FlomadAIplanner.ai.functions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public class AbstractLocationFunction {

    protected String getSessionIdFromHttpRequest() {
        // Retrieve the current HTTP request attributes
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attributes != null;
        HttpServletRequest httpRequest = attributes.getRequest();
        // Assuming the message is a request parameter or part of the request body
        String sessionId = httpRequest.getParameter("sessionId");
        log.info("Session ID: " + sessionId);
        return sessionId;
    }
}
