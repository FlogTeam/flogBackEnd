package golf.flogbackend.domain.flightLog.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;


@Getter
public class FlightLogResponseDto extends FlightLogDtoBase {

    public record CrewDto(String crewName) {}
    public record DutyDto(String duty, Long count) {}

    @Getter
    @Builder
    public static class DutyByAircraftTypeDto {
        private String aircraftType;
        private Long dutyTotalCount;
        private List<DutyDto> dutyByAircraftType;
    }

    @Getter
    @Builder
    public static class VisitedDataDto {
        private String name;
        private String code;
        private String nameKorean;
        private Long count;
        private Double percentage;
    }

    @Getter
    @Builder
    public static class CrewMateDto {
        private String name;
        private Long count;
    }

    @Getter
    @Builder
    public static class MostVisitedDataDto {
        private List<VisitedDataDto> mostVisitedDeparture;
        private List<VisitedDataDto> mostVisitedArrival;
        private List<VisitedDataDto> mostVisitedTotal;
    }

    @Getter
    @Builder
    public static class FlightLogDataDto {
        private Long workDays;
        private Long legCount;
        private Long dhCount;
        private FlightTimeDto totalFlightTime;
        private MostVisitedDataDto mostVisitedAirport;
        private MostVisitedDataDto mostVisitedCities;
        private MostVisitedDataDto mostVisitedCountries;
        private List<DutyByAircraftTypeDto> dutyByAircraftType;
        private List<CrewMateDto> crewMate;
    }

    @Getter
    @Builder
    public static class EtcInfoDto {
        private String duty;
        private List<CrewDto> crewMembers;
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

    @Getter
    @SuperBuilder
    public static class FlightResponseDto extends FlightCommonDto {
    }
}