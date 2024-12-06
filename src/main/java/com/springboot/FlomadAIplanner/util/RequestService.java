package com.springboot.FlomadAIplanner.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
public class RequestService {

    /**
     * Retrieves a parameter value from the current HTTP request based on the provided parameter name.
     *
     * @param paramName the name of the parameter to retrieve
     * @return the parameter value or null if not found or if no request is bound to the thread
     */
    public String getParameterFromRequest(String paramName) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            log.warn("No request bound to the current thread");
            return null; // or throw a custom exception depending on how you want to handle this case
        }

        HttpServletRequest request = attributes.getRequest();
        String paramValue = request.getParameter(paramName);

        if (paramValue != null) {
            log.info("Retrieved '{}' with value: {}", paramName, paramValue);
        } else {
            log.warn("Parameter '{}' not found in the request", paramName);
        }

        return paramValue;
    }
}
