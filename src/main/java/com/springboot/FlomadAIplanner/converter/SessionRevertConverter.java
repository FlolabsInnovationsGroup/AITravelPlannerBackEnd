package com.springboot.FlomadAIplanner.converter;

import com.springboot.FlomadAIplanner.dto.SessionDTO;
import com.springboot.FlomadAIplanner.model.entities.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class SessionRevertConverter implements Converter<SessionDTO, Session> {

    @Override
    public Session convert(SessionDTO source) {
        if (source == null) {
            log.info("Source session is 'null'");
            return null;
        }
        log.info("Converting session DTO to entity: {}", source.getSessionId());

        // Assuming userType is either a field in SessionDTO or default to "guest"
        String userType = source.getUserType() != null ? source.getUserType() : "guest";

        return new Session(
                source.getSessionId(),
                source.getUserId(),
                source.getTitle(),
                source.getCreatedAt() != null ? source.getCreatedAt() : LocalDateTime.now(),
                source.getUpdatedAt() != null ? source.getUpdatedAt() : LocalDateTime.now(),
                userType  // Adding userType to match Session constructor
        );
    }
}

