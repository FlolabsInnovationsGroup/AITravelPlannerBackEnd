package com.springboot.FlomadAIplanner.model.entities;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Document(collection = "destinations")
public class Destination extends Model {
    @Serial
    private static final long serialVersionUID = -8153912516789493415L;

    @Field("sortOrder")
    @NotNull
    private Integer sortOrder;

    @Field("destinationId")
    @EqualsAndHashCode.Include
    @Indexed(unique = true)
    @NotNull
    private Integer destinationId;

    @Field("destinationName")
    @NotBlank
    private String destinationName;

    @Field("selectable")
    private Boolean selectable;

    @Field("destinationUrlName")
    private String destinationUrlName;

    @Field("defaultCurrencyCode")
    private String defaultCurrencyCode;

    @Field("lookupId")
    private String lookupId;

    @Field("parentId")
    @EqualsAndHashCode.Include
    private Integer parentId;

    @Field("timeZone")
    private String timeZone;

    @Field("iataCode")
    @EqualsAndHashCode.Include
    private String iataCode;

    @Field("destinationType")
    @EqualsAndHashCode.Include
    @NotBlank
    private String destinationType;

    @Field("latitude")
    private Float latitude;

    @Field("longitude")
    private Float longitude;
}
