package golf.flogbackend.domain.flightLog.controller;

import golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto;
import golf.flogbackend.domain.flightLog.dto.FlightLogSummaryResponseDto;
import golf.flogbackend.domain.flightLog.dto.SaveFlightLogRequestDto;
import golf.flogbackend.domain.flightLog.dto.UpdateFlightLogRequestDto;
import golf.flogbackend.domain.flightLog.service.FlightLogGetService;
import golf.flogbackend.domain.flightLog.service.FlightLogService;
import golf.flogbackend.security.userDetails.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FlightLogController {
    private final FlightLogService flightLogService;
    private final FlightLogGetService flightLogGetService;

    @PostMapping("/flight-log")
    public ResponseEntity<FlightLogResponseDto.FlightLogSaveResponseDto> saveFlightLog(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                                      @RequestBody SaveFlightLogRequestDto saveFlightLogRequestDto) throws ParseException {
        return flightLogService.saveFlightLog(userDetails.getMember(), saveFlightLogRequestDto);
    }

    @PutMapping("/flight-log")
    public ResponseEntity<FlightLogResponseDto.FlightLogAllInfoDto> saveFlightLog(@AuthenticationPrincipal UserDetailsImpl userDetail,
                                                                                  @RequestBody UpdateFlightLogRequestDto updateFlightLogRequestDto) {
        return flightLogService.updateFlightLog(userDetail.getMember(), updateFlightLogRequestDto);
    }

    @GetMapping("/flight-log")
    public ResponseEntity<List<FlightLogSummaryResponseDto>> getFlightLogList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return flightLogGetService.getFlightLogList(userDetails.getMember());
    }

    @GetMapping("/flight-log/{flight-log-id}")
    public ResponseEntity<FlightLogResponseDto.FlightLogAllInfoDto>  getFlightLog(@PathVariable("flight-log-id") Long flightLogId) {
        return flightLogGetService.getFlightLog(flightLogId);
    }
}
