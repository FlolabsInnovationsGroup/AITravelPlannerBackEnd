package com.springboot.FlomadAIplanner.converter;

import com.springboot.FlomadAIplanner.dto.MessageDTO;
import com.springboot.FlomadAIplanner.model.entities.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MessageRevertConverter implements Converter<MessageDTO, Message> {

    public Message convert(MessageDTO source) {
        if (source == null) {
            log.info("Source message is 'null'");
            return null;
        }
        log.info("Converting message to entity: {}", source.getContent().substring(0, 20).concat("..."));

        Map<String, Object> metadata = null;
        if (source.getMetadata() != null) {
            metadata = source.getMetadata().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue
                    ));
        }

        return new Message(
                source.getId() != null ? String.valueOf(UUID.fromString(source.getId())) : null,
                source.getSessionId(),
                source.getContent(),
                source.getCreatedAt() != null ? source.getCreatedAt() : LocalDateTime.now(),
                source.getMessageType(),
                metadata
        );
    }
}
