package com.springboot.FlomadAIplanner.model.entities.login;

import com.springboot.FlomadAIplanner.model.entities.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Document(collection = "verification_tokens")
public class VerificationToken extends Model {
    @Serial
    private static final long serialVersionUID = -868559293333176630L;

    @DBRef
    private User user;
}
