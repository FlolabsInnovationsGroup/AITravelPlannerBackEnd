package com.springboot.FlomadAIplanner.converter;

import com.springboot.FlomadAIplanner.model.entities.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
        log.info("Converting message to entity: {}", StringUtils.substring(source.getText(), 0, 50));

        return Message.builder()
                .content(source.getText())
                .messageType(source.getMessageType())
                .metadata(source.getMetadata())
                .build();
    }
}
