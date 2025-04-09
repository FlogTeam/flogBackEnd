package golf.flogbackend.domain.flightLog.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
@Getter
public class UpdateFlightLogRequestDto {
    private Long flightLogId;
    private String duty;
    private List<String> crewMembers;

    private String airline;

    //aircraft info
    private String aircraftNumber;
    private String aircraftType;


    //departure info
    private LocalDate depDateUtc;
    private LocalDate depDateLocal;
    private String depAirportCode;
    private LocalTime depScheduledTimeUtc;
    private LocalTime depScheduledTimeLocal;
    private LocalTime depActualTimeUtc;
    private LocalTime depActualTimeLocal;

    //arrival info
    private LocalDate arrivalDateUtc;
    private LocalDate arrivalDateLocal;
    private String arrivalAirportCode;
    private LocalTime arrivalScheduledTimeUtc;
    private LocalTime arrivalScheduledTimeLocal;
    private LocalTime arrivalActualTimeUtc;
    private LocalTime arrivalActualTimeLocal;
}
