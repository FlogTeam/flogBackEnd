package golf.flogbackend.domain.flightLog.support;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.TimeZone;

public interface FlightEndpoint {
    LocalDate getDateUtc();
    LocalDate getDateLocal();
    LocalTime getScheduledTimeUtc();
    LocalTime getScheduledTimeLocal();
    LocalTime getActualTimeUtc();
    LocalTime getActualTimeLocal();
    String getAirportCode();
    String getAirportName();
    String getCountryCode();
    String getCountryName();
    Double getAirportLocationLat();
    Double getAirportLocationLon();
    TimeZone getAirportTimezone();
}
