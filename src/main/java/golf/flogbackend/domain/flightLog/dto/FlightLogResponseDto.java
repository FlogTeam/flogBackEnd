package golf.flogbackend.domain.flightLog.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.TimeZone;


@Getter
public class FlightLogResponseDto {
    public record ScheduledTimeDto(LocalTime scheduledTimeUtc, LocalTime scheduledTimeLocal) {}
    public record ActualTimeDto(LocalTime actualTimeUtc, LocalTime actualTimeLocal) {}
    public record DateInfoDto(LocalDate dateUtc, LocalDate dateLocal) {}
    public record LocationDto(Double latitude, Double longitude) {}
    public record CityDto(String cityCode, String cityName) {}
    public record AirportDto(String airportCode, String airportName, String airportNameKorean) {}
    public record CountryDto(String countryCode, String countryName,
                             String countryNameKorean, String region) {}
    public record AircraftDto(String aircraftNumber, String aircraftType) {}
    public record FlightInfoDto(String flightId, String airline) {}
    public record CrewDto(String crewName) {}

    @Getter
    @Builder
    public static class DepartureDto {
        private DateInfoDto dateInfo;
        private AirportDto airport;
        private LocationDto location;
        private TimeZone timeZone;
        private ScheduledTimeDto scheduledTime;
        private ActualTimeDto actualTime;
        private CountryDto country;
        private CityDto city;
    }

    @Getter
    @Builder
    public static class ArrivalDto {
        private DateInfoDto dateInfo;
        private AirportDto airport;
        private LocationDto location;
        private TimeZone timeZone;
        private ScheduledTimeDto scheduledTime;
        private ActualTimeDto actualTime;
        private CountryDto country;
        private CityDto city;
    }

    @Getter
    @Builder
    public static class EtcInfoDto {
        private String duty;
        private List<CrewDto> crewMembers;
    }

    @Getter
    public static class FlightTimeDto {
        private int hours;
        private int minutes;
        private int seconds;

        public FlightTimeDto(Long totalSeconds) {
            if (totalSeconds == null) {
                this.hours = 0;
                this.minutes = 0;
                this.seconds = 0;
            } else {
                this.hours = (int) (totalSeconds / 3600);
                int remainingSeconds = (int) (totalSeconds % 3600);
                this.minutes = remainingSeconds / 60;
                this.seconds = remainingSeconds % 60;
            }
        }
    }

    @Getter
    @Builder
    public static class DistanceDto {
        private Double distanceKilometers;
        private Double distanceMeters;
        private Double distanceMiles;
    }

    @Getter
    @Builder
    public static class FlightLogSaveResponseDto {
        private Long flightLogId;
        private FlightInfoDto flightInfo;
        private DepartureDto departure;
        private ArrivalDto arrival;
        private AircraftDto aircraft;
    }

    @Getter
    @Builder
    public static class FlightLogAllInfoDto {
        private Long flightLogId;
        private String memberId;
        private FlightInfoDto flightInfo;
        private DepartureDto departure;
        private ArrivalDto arrival;
        private AircraftDto aircraft;
        private DistanceDto distance;
        private FlightTimeDto flightTime;
        private EtcInfoDto etcInfo;
    }
}