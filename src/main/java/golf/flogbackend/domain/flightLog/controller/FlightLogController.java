package golf.flogbackend.domain.flightLog.controller;

import golf.flogbackend.domain.flightLog.dto.FlightLogRequestDto;
import golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto;
import golf.flogbackend.domain.flightLog.dto.FlightLogSummaryResponseDto;
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

    @GetMapping("/flight-log/{flight-id}/{flight-date}")
    public ResponseEntity<?> getFlightList(@PathVariable("flight-id") String flightId, @PathVariable("flight-date") String flightDate) throws ParseException {
        return flightLogGetService.getFlightList(flightId, flightDate);
    }

    @PostMapping("/flight-log")
    public ResponseEntity<FlightLogResponseDto.FlightLogAllInfoDto> saveFlightLog(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                                       @RequestBody FlightLogRequestDto flightLogRequestDto) {
        return flightLogService.createFlightLog(userDetails.getMember(), flightLogRequestDto);
    }

    @PutMapping("/flight-log")
    public ResponseEntity<FlightLogResponseDto.FlightLogAllInfoDto> saveFlightLog(@AuthenticationPrincipal UserDetailsImpl userDetail,
                                                                                  @RequestBody UpdateFlightLogRequestDto updateFlightLogRequestDto) {
        return flightLogService.updateFlightLog(userDetail.getMember(), updateFlightLogRequestDto);
    }

    @GetMapping("/flight-log/summary")
    public ResponseEntity<List<FlightLogSummaryResponseDto>> getFlightLogList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return flightLogGetService.getFlightLogSummaryList(userDetails.getMember());
    }

    @GetMapping("/flight-log/details/{flight-log-id}")
    public ResponseEntity<FlightLogResponseDto.FlightLogAllInfoDto> getFlightLog(@PathVariable("flight-log-id") Long flightLogId) {
        return flightLogGetService.getFlightLog(flightLogId);
    }

    @GetMapping("/flight-log/data")
    public ResponseEntity<FlightLogResponseDto.FlightLogDataDto> getFlightLogData(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                                  @RequestParam(value = "start-date", required = false) String startDate,
                                                                                  @RequestParam(value = "end-date", required = false) String endDate) {
        return flightLogGetService.getFlightLogData(userDetails.getMember(), startDate, endDate);
    }
}
