package com.springboot.FlomadAIplanner.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "destinations")
public class Destination {

    @Id
    @Column(name = "sortOrder")
    private Integer sortOrder;

    @Column(name = "destinationId")
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
    private Integer parentId;

    @Column(name = "timeZone")
    private String timeZone;

    @Column(name = "iataCode")
    private String iataCode;

    @Column(name = "destinationType")
    private String destinationType;

    @Column(name = "latitude")
    private Float latitude;

    @Column(name = "longitude")
    private Float longitude;

}
