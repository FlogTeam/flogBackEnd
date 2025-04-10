package golf.flogbackend.domain.flightLog.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.TimeZone;


@Getter
public class FlightLogResponseDto {
    @Getter
    @Builder
    public static class ScheduledTimeDto {
        private LocalTime scheduledTimeUtc;
        private LocalTime scheduledTimeLocal;
    }

    @Getter
    @Builder
    public static class ActualTimeDto {
        private LocalTime actualTimeUtc;
        private LocalTime actualTimeLocal;
    }

    @Getter
    @Builder
    public static class DateInfoDto {
        private LocalDate dateUtc;
        private LocalDate dateLocal;
    }

    @Getter
    @Builder
    public static class LocationDto {
        private Double longitude;
        private Double latitude;
    }

    @Getter
    @Builder
    public static class AirportDto {
        private String airportCode;
        private String airportName;
    }

    @Getter
    @Builder
    public static class CountryDto {
        private String countryCode;
        private String countryName;
    }

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
    }

    @Getter
    @Builder
    public static class AircraftDto {
        private String aircraftNumber;
        private String aircraftType;
    }

    @Getter
    @Builder
    public static class FlightInfoDto {
        private String flightId;
        private String airline;
    }

    @Getter
    @Builder
    public static class CrewDto {
        private String crewName;
    }

    @Getter
    @Builder
    public static class EtcInfoDto {
        private String duty;
        private List<CrewDto> crewMembers;
    }

    //summary
    @Getter
    public static class FlightTimeDto {
        private int hours;
        private int minutes;
        private int seconds;

        public FlightTimeDto(long totalSeconds) {
            this.hours = (int) (totalSeconds / 3600);
            int remainingSeconds = (int) (totalSeconds % 3600);
            this.minutes = remainingSeconds / 60;
            this.seconds = remainingSeconds % 60;
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