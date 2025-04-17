package golf.flogbackend.domain.flightLog.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.TimeZone;


@Getter
public class FlightLogResponseDto {
    public record ScheduledTimeDto(LocalTime utc, LocalTime local) {}
    public record ActualTimeDto(LocalTime utc, LocalTime local) {}
    public record DateInfoDto(LocalDate utc, LocalDate local) {}
    public record LocationDto(Double latitude, Double longitude) {}
    public record CityDto(String code, String name) {}
    public record AirportDto(String code, String name, String nameKorean) {}
    public record CountryDto(String code, String name, String nameKorean, String region) {}
    public record AircraftDto(String number, String type) {}
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
        private Double kilometers;
        private Double meters;
        private Double miles;
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