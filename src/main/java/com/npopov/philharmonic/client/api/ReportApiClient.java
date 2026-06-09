package com.npopov.philharmonic.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.npopov.philharmonic.client.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportApiClient extends BaseApiClient {
    private static final ReportApiClient INSTANCE = new ReportApiClient();
    public static ReportApiClient getInstance() { return INSTANCE; }
    private ReportApiClient() {}

    public List<ArtistModel> getArtistsNoCompetitions(LocalDateTime start, LocalDateTime end) {
        String path = String.format("/api/reports/artists-no-competitions?start=%s&end=%s",
                start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return getList(path, new TypeReference<>() {});
    }

    public List<OrganizerStat> getOrganizerStats(LocalDateTime start, LocalDateTime end) {
        String path = String.format("/api/reports/organizers-stats?start=%s&end=%s",
                start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return getList(path, new TypeReference<>() {});
    }

    public List<VenueWithEvent> getVenuesWithEvents(LocalDateTime start, LocalDateTime end) {
        String path = String.format("/api/reports/venues-with-events?start=%s&end=%s",
                start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return getList(path, new TypeReference<>() {});
    }
}