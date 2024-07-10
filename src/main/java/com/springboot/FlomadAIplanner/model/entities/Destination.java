package com.springboot.FlomadAIplanner.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Entity
@Table(name = "destinations")
public class Destination extends Model {

    @Column(name = "sortOrder")
    private Integer sortOrder;

    @Column(name = "destinationId")
    @EqualsAndHashCode.Include
    private Integer destinationId;

    @Column(name = "destinationName")
    private String destinationName;

    @Column(name = "selectable")
    private Boolean selectable;

    @Column(name = "destinationUrlName")
    private String destinationUrlName;

    @Column(name = "defaultCurrencyCode")
    private String defaultCurrencyCode;

    @Column(name = "lookupId")
    private String lookupId;

    @Column(name = "parentId")
    @EqualsAndHashCode.Include
    private Integer parentId;

    @Column(name = "timeZone")
    private String timeZone;

    @Column(name = "iataCode")
    @EqualsAndHashCode.Include
    private String iataCode;

    @Column(name = "destinationType")
    @EqualsAndHashCode.Include
    private String destinationType;

    @Column(name = "latitude")
    private Float latitude;

    @Column(name = "longitude")
    private Float longitude;

}
