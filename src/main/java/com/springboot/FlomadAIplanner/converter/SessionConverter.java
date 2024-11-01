package com.springboot.FlomadAIplanner.converter;

import com.springboot.FlomadAIplanner.dto.SessionDTO;
import com.springboot.FlomadAIplanner.model.entities.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class SessionConverter implements Converter<Session, SessionDTO> {

    @Override
    public SessionDTO convert(Session source) {
        if (source == null) {
            log.info("Source session is 'null'");
            return null;
        }
        log.info("Converting session entity to DTO: {}", source.getId());

        return new SessionDTO(
                source.getId(),
                source.getUserId(),
                source.getTitle(),
                source.getCreatedAt() != null ? source.getCreatedAt() : LocalDateTime.now(),
                source.getUpdatedAt() != null ? source.getUpdatedAt() : LocalDateTime.now(),
                source.getUserType()  // Assuming userType is handled here correctly
        );
    }
}


