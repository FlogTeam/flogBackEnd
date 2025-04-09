package golf.flogbackend.domain.flightLog.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class SaveFlightLogRequestDto {
    private String flightId;
    private LocalDate flightDate;
}
