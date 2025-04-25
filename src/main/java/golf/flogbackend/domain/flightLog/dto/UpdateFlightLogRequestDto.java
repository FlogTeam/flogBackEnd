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
    private String depDateActualUtc;
    private String depDateActualLocal;
    private String depDateScheduledUtc;
    private String depDateScheduledLocal;
    private String depAirportCode;
    private String depScheduledTimeUtc;
    private String depScheduledTimeLocal;
    private String depActualTimeUtc;
    private String depActualTimeLocal;

    //arrival info
    private String arrivalDateActualUtc;
    private String arrivalDateActualLocal;
    private String arrivalDateScheduledUtc;
    private String arrivalDateScheduledLocal;
    private String arrivalAirportCode;
    private String arrivalScheduledTimeUtc;
    private String arrivalScheduledTimeLocal;
    private String arrivalActualTimeUtc;
    private String arrivalActualTimeLocal;
}
