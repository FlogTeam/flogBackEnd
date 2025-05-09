package golf.flogbackend.domain.flightLog.support;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.TimeZone;

public interface FlightEndpoint {
    LocalDate getDateActualUtc();
    LocalDate getDateActualLocal();
    LocalDate getDateScheduledUtc();
    LocalDate getDateScheduledLocal();
    LocalTime getScheduledTimeUtc();
    LocalTime getScheduledTimeLocal();
    LocalTime getActualTimeUtc();
    LocalTime getActualTimeLocal();
    String getAirportCode();
    String getAirportName();
    String getAirportNameKorean();
    String getRegion();
    String getCountryCode();
    String getCountryName();
    String getCountryNameKorean();
    String getCityCode();
    String getCityName();
    String getCityNameKorean();
    Double getAirportLocationLat();
    Double getAirportLocationLon();
    TimeZone getAirportTimezone();
}
