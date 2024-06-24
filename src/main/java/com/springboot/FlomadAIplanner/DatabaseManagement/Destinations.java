package com.springboot.FlomadAIplanner.DatabaseManagement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "destinations")
public class Destinations {

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

    // Getters
    public Integer getDestinationId() {
        return destinationId;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Boolean isSelectable() {
        return selectable;
    }

    public String getDestinationUrlName() {
        return destinationUrlName;
    }

    public String getDefaultCurrencyCode() {
        return defaultCurrencyCode;
    }

    public String getLookupId() {
        return lookupId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public String getIataCode() {
        return iataCode;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public Float getLatitude() {
        return latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    // Setters
    public void setDestinationId(Integer destinationId) {
        this.destinationId = destinationId;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setSelectable(Boolean selectable) {
        this.selectable = selectable;
    }

    public void setDestinationUrlName(String destinationUrlName) {
        this.destinationUrlName = destinationUrlName;
    }

    public void setDefaultCurrencyCode(String defaultCurrencyCode) {
        this.defaultCurrencyCode = defaultCurrencyCode;
    }

    public void setLookupId(String lookupId) {
        this.lookupId = lookupId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public void setIataCode(String iataCode) {
        this.iataCode = iataCode;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

}
