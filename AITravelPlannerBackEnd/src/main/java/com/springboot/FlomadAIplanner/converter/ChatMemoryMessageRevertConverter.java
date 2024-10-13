package com.springboot.FlomadAIplanner.converter;

import com.springboot.FlomadAIplanner.model.entities.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ChatMemoryMessageRevertConverter implements Converter<Message, org.springframework.ai.chat.messages.Message> {

    @Override
    public org.springframework.ai.chat.messages.Message convert(Message source) {
        if (source == null) {
            log.info("Source message is 'null'");
            return null;
        }
        log.info("Converting message to DTO: {}", StringUtils.substring(source.getContent(), 0, 50));

        Map<String, Object> metadata = new HashMap<>();
        if (source.getMetadata() != null) {
            metadata = source.getMetadata().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue
                    ));
        }
        metadata.put("createdAt", source.getCreatedAt());


        Map<String, Object> finalMetadata = metadata;
        return new org.springframework.ai.chat.messages.Message() {
            @Override
            public String getContent() {
                return source.getContent();
            }

            @SuppressWarnings("all")
            @Override
            public List<Media> getMedia(String... dummy) {
                return null;
            }

            @Override
            public MessageType getMessageType() {
                return source.getMessageType();
            }

            @Override
            public Map<String, Object> getMetadata() {
                return finalMetadata;
            }
        };
    }

}