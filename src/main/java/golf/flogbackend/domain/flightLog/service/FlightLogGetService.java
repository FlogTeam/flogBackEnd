package golf.flogbackend.domain.flightLog.service;

import golf.flogbackend.domain.crew.entity.Crew;
import golf.flogbackend.domain.crew.repository.CrewRepository;
import golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto;
import golf.flogbackend.domain.flightLog.dto.FlightLogSummaryResponseDto;
import golf.flogbackend.domain.flightLog.entity.FlightLog;
import golf.flogbackend.domain.flightLog.repository.FlightLogRepository;
import golf.flogbackend.domain.flightLog.support.DutyCountByAircraftType;
import golf.flogbackend.domain.flightLog.support.FlightLogResponseDtoMapper;
import golf.flogbackend.domain.flightLog.support.FlightLogUtil;
import golf.flogbackend.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static golf.flogbackend.domain.flightLog.support.FlightLogResponseDtoMapper.buildFlightLogAllInfoDto;
import static golf.flogbackend.domain.flightLog.support.FlightLogUtil.parseDateOrDefault;
import static golf.flogbackend.exception.ErrorCode.INVALID_DATE_ORDER;

@Service
@RequiredArgsConstructor
public class FlightLogGetService {
    private final FlightLogRepository flightLogRepository;
    private final CrewRepository crewRepository;
    private final FlightLogUtil flightLogUtil;

    public ResponseEntity<List<FlightLogSummaryResponseDto>> getFlightLogList(Member member) {
        return ResponseEntity.ok(flightLogRepository.findByMemberIdOrderByCreateAtDesc(member.getEmail())
                .stream().map(FlightLogResponseDtoMapper::buildFlightLogSummaryResponseDto)
                .toList());
    }

    public ResponseEntity<FlightLogResponseDto.FlightLogAllInfoDto> getFlightLog(Long flightLogId) {
        return ResponseEntity.ok(buildFlightLogAllInfoDto(
                flightLogUtil.findFlightLogById(flightLogId),
                crewRepository.findByFlightLogId(flightLogId)));
    }

    public ResponseEntity<FlightLogResponseDto.FlightLogDataDto> getFlightLogData(Member member, String startDate, String endDate) {
        LocalDate parsedStartDate = parseDateOrDefault(startDate, LocalDate.EPOCH);
        LocalDate parsedEndDate = parseDateOrDefault(endDate, LocalDate.now());
        if (!parsedStartDate.isBefore(parsedEndDate))
            throw new IllegalArgumentException(INVALID_DATE_ORDER.code() + "startDate must be before endDate");

        List<FlightLog> flightLogList = flightLogRepository.findByMemberIdAndFlightDateBetween(member.getEmail(), parsedStartDate, parsedEndDate);

        return ResponseEntity.ok(FlightLogResponseDtoMapper.buildFlightLogDataDto(
                flightLogList,
                flightLogRepository.findDutyStatsGroupedByAircraftType(member.getEmail(), parsedStartDate, parsedEndDate).stream()
                        .collect(Collectors.groupingBy(DutyCountByAircraftType::getAircraftType))
                        .entrySet()
                        .stream()
                        .sorted(Comparator.comparingLong(
                                (Map.Entry<String, List<DutyCountByAircraftType>> entry) ->
                                        entry.getValue().stream()
                                                .mapToLong(DutyCountByAircraftType::getCount)
                                                .sum()
                        ).reversed())
                        .map(entry -> FlightLogResponseDto.DutyByAircraftTypeDto.builder()
                                .aircraftType(entry.getKey())
                                .dutyTotalCount(entry.getValue()
                                        .stream()
                                        .mapToLong(DutyCountByAircraftType::getCount).sum())
                                .dutyByAircraftType(
                                        entry.getValue().stream()
                                                .sorted(Comparator.comparingLong(DutyCountByAircraftType::getCount).reversed())
                                                .map(d -> new FlightLogResponseDto.DutyDto(
                                                        d.getDuty() != null ? d.getDuty() : "UNKNOWN", d.getCount()))
                                                .toList()
                                )
                                .build()
                        )
                        .toList(),
                crewRepository.findByEmail(member.getEmail()).stream()
                        .collect(Collectors.groupingBy(Crew::getName, Collectors.counting()))
                        .entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .map(e -> FlightLogResponseDto.CrewMateDto.builder()
                                .name(e.getKey())
                                .count(e.getValue())
                                .build())
                        .toList()));
    }
}
