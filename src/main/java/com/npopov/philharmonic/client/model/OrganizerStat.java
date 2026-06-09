package com.npopov.philharmonic.client.model;

public class OrganizerStat {
    private Long id;
    private String name;
    private Long eventsCount;

    public OrganizerStat(Long id, String name, Long eventsCount) {
        this.id = id;
        this.name = name;
        this.eventsCount = eventsCount;
    }

    public OrganizerStat() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getEventsCount() {
        return eventsCount;
    }

    public void setEventsCount(Long eventsCount) {
        this.eventsCount = eventsCount;
    }
}
