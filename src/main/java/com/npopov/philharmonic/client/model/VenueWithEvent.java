package com.npopov.philharmonic.client.model;

import java.time.LocalDateTime;

public class VenueWithEvent {
    private Long venueId;
    private String venueName;
    private Long eventId;
    private String eventTitle;
    private LocalDateTime eventDate;

    public VenueWithEvent(Long venueId, String venueName, Long eventId, String eventTitle, LocalDateTime eventDate) {
        this.venueId = venueId;
        this.venueName = venueName;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.eventDate = eventDate;
    }

    public VenueWithEvent() {
    }

    public Long getVenueId() {
        return venueId;
    }

    public void setVenueId(Long venueId) {
        this.venueId = venueId;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }
}
