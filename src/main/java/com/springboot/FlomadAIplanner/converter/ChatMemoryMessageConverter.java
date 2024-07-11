package com.springboot.FlomadAIplanner.converter;

import com.springboot.FlomadAIplanner.model.entities.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatMemoryMessageConverter implements Converter<org.springframework.ai.chat.messages.Message, Message> {

    @Override
    public Message convert(org.springframework.ai.chat.messages.Message source) {
        if (source == null) {
            log.info("Source message is 'null'");
            return null;
        }
        log.info("Converting message to entity: {}", source.getContent().substring(0, 20).concat("..."));

        return Message.builder()
                .content(source.getContent())
                .messageType(source.getMessageType())
                .metadata(source.getMetadata())
                .build();
    }
}
