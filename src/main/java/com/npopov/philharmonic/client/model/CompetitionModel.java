package com.npopov.philharmonic.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompetitionModel {
    private Long id;
    private Long eventId;
    private String competitionType;
    private String rules;
    private String juryInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CompetitionModel() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public String getCompetitionType() { return competitionType; }
    public void setCompetitionType(String competitionType) { this.competitionType = competitionType; }
    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }
    public String getJuryInfo() { return juryInfo; }
    public void setJuryInfo(String juryInfo) { this.juryInfo = juryInfo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
