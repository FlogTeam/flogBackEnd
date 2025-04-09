package golf.flogbackend.domain.flightLog.controller;

import golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto;
import golf.flogbackend.domain.flightLog.dto.SaveFlightLogRequestDto;
import golf.flogbackend.domain.flightLog.dto.UpdateFlightLogRequestDto;
import golf.flogbackend.domain.flightLog.service.FlightLogService;
import golf.flogbackend.security.userDetails.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FlightLogController {
    private final FlightLogService flightLogService;

    @PostMapping("/flight-log")
    public ResponseEntity<FlightLogResponseDto.StepOneResponseDto> addFlightLog(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                                @RequestBody SaveFlightLogRequestDto saveFlightLogRequestDto) throws ParseException {
        return flightLogService.saveFlightLogStepOne(userDetails.getMember(), saveFlightLogRequestDto);
    }

    @PutMapping("/flight-log")
    public ResponseEntity<FlightLogResponseDto.AggregateDto> saveFlightLog(@AuthenticationPrincipal UserDetailsImpl userDetail,
                                                                           @RequestBody UpdateFlightLogRequestDto updateFlightLogRequestDto) {
        return flightLogService.updateFlightLog(userDetail.getMember(), updateFlightLogRequestDto);
    }
}
