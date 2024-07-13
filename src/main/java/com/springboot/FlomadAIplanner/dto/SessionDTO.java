package com.springboot.FlomadAIplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


/**
 * @class
 * @description This is a class store data retrived from database(all messages in a session) and ready to send to front end
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SessionDTO {
    @EqualsAndHashCode.Include
    private String id;
    private String userId;
    private String title;
    private LocalDateTime timestamp;

}
