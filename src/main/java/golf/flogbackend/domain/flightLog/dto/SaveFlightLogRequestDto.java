package golf.flogbackend.domain.flightLog.dto;

import lombok.Getter;

@Getter
public class SaveFlightLogRequestDto {
    private String flightId;
    private String flightDate;
}
