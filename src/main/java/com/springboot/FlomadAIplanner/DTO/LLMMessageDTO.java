package com.springboot.FlomadAIplanner.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LLMMessageDTO {
    private String sender;
    private String message;

}
