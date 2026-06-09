package com.npopov.philharmonic.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VenueModel {
    private Long id;
    private String name;
    private String venueType;
    private String address;
    private String city;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Поля для Theatre
    private Integer capacity;
    private Integer stageWidthMm;
    private Integer stageDepthMm;

    // Поля для Cinema
    private Integer screenWidthMm;
    private Integer screenHeightMm;
    private Integer screenDiagonalMm;
    private String  screenAspectRatio;

    // Поля для ConcertVenue
    private String  stageType;
    private Boolean hasSoundSystem;

    // Поля для CulturalCentre
    private Integer communityRoomsCount;

    // Поля для VarietyStage
    private String  genreFocus;

    public VenueModel() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVenueType() { return venueType; }
    public void setVenueType(String venueType) { this.venueType = venueType; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Integer getStageWidthMm() { return stageWidthMm; }

    public void setStageWidthMm(Integer stageWidthMm) {
        this.stageWidthMm = stageWidthMm;
    }

    public Integer getStageDepthMm() {
        return stageDepthMm;
    }

    public void setStageDepthMm(Integer stageDepthMm) {
        this.stageDepthMm = stageDepthMm;
    }

    public Integer getScreenWidthMm() {
        return screenWidthMm;
    }

    public void setScreenWidthMm(Integer screenWidthMm) {
        this.screenWidthMm = screenWidthMm;
    }

    public Integer getScreenHeightMm() {
        return screenHeightMm;
    }

    public void setScreenHeightMm(Integer screenHeightMm) {
        this.screenHeightMm = screenHeightMm;
    }

    public Integer getScreenDiagonalMm() {
        return screenDiagonalMm;
    }

    public void setScreenDiagonalMm(Integer screenDiagonalMm) {
        this.screenDiagonalMm = screenDiagonalMm;
    }

    public String getScreenAspectRatio() {
        return screenAspectRatio;
    }

    public void setScreenAspectRatio(String screenAspectRatio) {
        this.screenAspectRatio = screenAspectRatio;
    }

    public String getStageType() {
        return stageType;
    }

    public void setStageType(String stageType) {
        this.stageType = stageType;
    }

    public Boolean getHasSoundSystem() {
        return hasSoundSystem;
    }

    public void setHasSoundSystem(Boolean hasSoundSystem) {
        this.hasSoundSystem = hasSoundSystem;
    }

    public Integer getCommunityRoomsCount() {
        return communityRoomsCount;
    }

    public void setCommunityRoomsCount(Integer communityRoomsCount) {
        this.communityRoomsCount = communityRoomsCount;
    }

    public String getGenreFocus() {
        return genreFocus;
    }

    public void setGenreFocus(String genreFocus) {
        this.genreFocus = genreFocus;
    }
}
