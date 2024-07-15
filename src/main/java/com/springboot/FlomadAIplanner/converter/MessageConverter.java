package com.springboot.FlomadAIplanner.converter;

import com.springboot.FlomadAIplanner.dto.MessageDTO;
import com.springboot.FlomadAIplanner.model.entities.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MessageConverter implements Converter<Message, MessageDTO> {

    @Override
    public MessageDTO convert(Message source) {
        if (source == null) {
            log.info("Source message is 'null'");
            return null;
        }
        log.info("Converting message to DTO: {}", StringUtils.substring(source.getContent(), 0, 50));

        Map<String, String> metadata = null;
        if (source.getMetadata() != null) {
            metadata = source.getMetadata().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().toString() // Assuming all values can be converted to String
                    ));
        }

        return new MessageDTO(
                source.getId(),
                source.getSessionId(),
                source.getContent(),
                source.getCreatedAt(),
                source.getMessageType(),
                metadata
        );
    }

}
