package kg.astra.checkin;

import kg.astra.health.CrewStatusResponse;

public record CheckinResponse(long checkinId, CrewStatusResponse status) {}
