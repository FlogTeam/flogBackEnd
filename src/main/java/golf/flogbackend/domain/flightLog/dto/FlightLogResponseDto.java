package golf.flogbackend.domain.flightLog.dto;

import golf.flogbackend.domain.crew.entity.Crew;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
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
    public static class RevisedTimeDto {
        private LocalTime RevisedTimeUtc;
        private LocalTime RevisedTimeLocal;
    }

    @Getter
    @Builder
    public static class RunawayTimeDto {
        private LocalTime runawayTimeUtc;
        private LocalTime runawayTimeLocal;
    }

    @Getter
    @Builder
    public static class PredictedTimeDto {
        private LocalTime predictedTimeUtc;
        private LocalTime predictedTimeLocal;
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
        private DateInfoDto dateInfoDto;
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
        private DateInfoDto dateInfoDto;
        private AirportDto airportDto;
        private LocationDto location;
        private TimeZone timeZone;
        private ScheduledTimeDto scheduledTime;
        private PredictedTimeDto predictedTime;
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

    @Getter
    @Builder
    public static class StepOneResponseDto {
        private Long flightLogId;
        private FlightInfoDto flightInfo;
        private DepartureDto departure;
        private ArrivalDto arrival;
        private AircraftDto aircraft;
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
        private EtcInfoDto  etcInfo;
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


}