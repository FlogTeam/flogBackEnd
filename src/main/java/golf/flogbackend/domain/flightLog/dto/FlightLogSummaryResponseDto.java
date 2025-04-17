package golf.flogbackend.domain.flightLog.dto;


import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Getter
public class FlightLogSummaryResponseDto {
    private Long flightLogId;
    private String flightId;
    private LocalDate flightDate;
    private FlightLogResponseDto.FlightTimeDto flightTime;
    private String airline;
    private String aircraftType;
    private DepartureDto departure;
    private ArrivalDto arrival;

    public record DepartureDto(String airportCode, LocalTime departureTimeLocal) {}
    public record ArrivalDto(String airportCode, LocalTime arrivalTimeLocal) {}
}
