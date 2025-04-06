package golf.flogbackend.domain.flightLog.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.TimeZone;


@Getter
public class FlightLogResponseDto {
    @Getter
    @Builder
    public static class ScheduledTimeDto {
        private ZonedDateTime scheduledTimeUtc;
        private ZonedDateTime scheduledTimeLocal;
    }

    @Getter
    @Builder
    public static class RevisedTimeDto {
        private ZonedDateTime RevisedTimeUtc;
        private ZonedDateTime RevisedTimeLocal;
    }

    @Getter
    @Builder
    public static class RunawayTimeDto {
        private ZonedDateTime runawayTimeUtc;
        private ZonedDateTime runawayTimeLocal;
    }

    @Getter
    @Builder
    public static class PredictedTimeDto {
        private ZonedDateTime predictedTimeUtc;
        private ZonedDateTime predictedTimeLocal;
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
        private AirportDto airportDto;
        private LocationDto location;
        private TimeZone timeZone;
        private ScheduledTimeDto scheduledTime;
        private RevisedTimeDto revisedTime;
        private RunawayTimeDto runawayTime;
        private CountryDto country;
    }

    @Getter
    @Builder
    public static class ArrivalDto {
        private AirportDto airportDto;
        private LocationDto location;
        private TimeZone timeZone;
        private ScheduledTimeDto scheduledTime;
        private PredictedTimeDto predictedTime;
        private CountryDto country;
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
    public static class AircraftDto {
        private String aircraftNumber;
        private String aircraftType;
    }

    @Getter
    @Builder
    public static class FlightInfoDto {
        private String flightId;
        private LocalDate flightDate;
        private String airline;
        private String duty;
    }

    @Getter
    @Builder
    public static class AggregateDto {
        private Long flightLogId;
        private String memberId;
        private FlightInfoDto flightInfo;
        private DepartureDto departure;
        private ArrivalDto arrival;
        private AircraftDto aircraft;
        private DistanceDto distance;
        private FlightTimeDto  flightTime;
    }

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
}