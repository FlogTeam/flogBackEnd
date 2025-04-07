package golf.flogbackend.domain.flightLog.controller;

import golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto;
import golf.flogbackend.domain.flightLog.dto.SaveFlightLogStepOneRequestDto;
import golf.flogbackend.domain.flightLog.service.FlightLogService;
import golf.flogbackend.security.userDetails.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FlightLogController {
    private final FlightLogService flightLogService;

    @PostMapping("/flight-log")
    public ResponseEntity<FlightLogResponseDto.AggregateDto> addFlightLog(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                             @RequestBody SaveFlightLogStepOneRequestDto saveFlightLogStepOneRequestDto) throws ParseException {
        return flightLogService.saveFlightLogStepOne(userDetails.getMember(), saveFlightLogStepOneRequestDto);
    }
}
