package golf.flogbackend.domain.flightLog.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.TimeZone;

public class FlightLogDtoBase {
    public record ScheduledTimeDto(LocalTime utc, LocalTime local) {}
    public record ActualTimeDto(LocalTime utc, LocalTime local) {}
    public record ScheduledDateInfoDto(LocalDate utc, LocalDate local) {}
    public record ActualDateInfoDto(LocalDate utc, LocalDate local) {}
    public record LocationDto(Double latitude, Double longitude) {}
    public record CityDto(String code, String name, String cityNameKorean) {}
    public record AirportDto(String code, String name, String nameKorean) {}
    public record CountryDto(String code, String name, String nameKorean, String region) {}
    public record AircraftDto(String number, String type) {}
    public record FlightInfoDto(String flightId, String airline, LocalDate flightDate) {}

    @Getter
    @Builder
    public static class DepartureDto {
        private ScheduledDateInfoDto scheduledDateInfo;
        private ActualDateInfoDto actualDateInfo;
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
        private ScheduledDateInfoDto scheduledDateInfo;
        private ActualDateInfoDto actualDateInfo;
        private AirportDto airport;
        private LocationDto location;
        private TimeZone timeZone;
        private ScheduledTimeDto scheduledTime;
        private ActualTimeDto actualTime;
        private CountryDto country;
        private CityDto city;
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
    @SuperBuilder
    @NoArgsConstructor
    public static class FlightCommonDto {
        private FlightInfoDto flightInfo;
        private DepartureDto departure;
        private ArrivalDto arrival;
        private AircraftDto aircraft;
        private DistanceDto distance;
    }
}
