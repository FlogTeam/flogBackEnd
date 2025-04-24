package golf.flogbackend.domain.flightLog.service;

import golf.flogbackend.domain.crew.repository.CrewRepository;
import golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto;
import golf.flogbackend.domain.flightLog.dto.FlightLogSummaryResponseDto;
import golf.flogbackend.domain.flightLog.entity.FlightLog;
import golf.flogbackend.domain.flightLog.repository.FlightLogRepository;
import golf.flogbackend.domain.flightLog.support.DutyCountByAircraftType;
import golf.flogbackend.domain.flightLog.support.FlightLogResponseDtoMapper;
import golf.flogbackend.domain.member.entity.Member;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static golf.flogbackend.domain.flightLog.support.FlightLogResponseDtoMapper.buildFlightLogAllInfoDto;

@Service
@RequiredArgsConstructor
public class FlightLogGetService {
    private final FlightLogRepository flightLogRepository;
    private final CrewRepository crewRepository;

    public ResponseEntity<List<FlightLogSummaryResponseDto>> getFlightLogList(Member member) {
        return ResponseEntity.ok(flightLogRepository.findByMemberIdOrderByCreateAtDesc(member.getEmail())
                .stream().map(FlightLogResponseDtoMapper::buildFlightLogSummaryResponseDto)
                .toList());
    }

    public ResponseEntity<FlightLogResponseDto.FlightLogAllInfoDto> getFlightLog(Long flightLogId) {
        return ResponseEntity.ok(buildFlightLogAllInfoDto(
                flightLogRepository.findById(flightLogId).orElseThrow(EntityNotFoundException::new),
                crewRepository.findByFlightLogId(flightLogId)));
    }

    public ResponseEntity<FlightLogResponseDto.FlightLogDataDto> getFlightLogData(Member member, LocalDate startDate, LocalDate endDate) {
        startDate = startDate == null ? LocalDate.EPOCH : startDate;
        endDate = endDate == null ? LocalDate.now() : endDate;
        if (!startDate.isBefore(endDate)) throw new IllegalArgumentException("startDate must be before endDate");

        List<FlightLog> flightLogList = flightLogRepository.findByMemberIdAndFlightDateBetween(member.getEmail(), startDate, endDate);


        return ResponseEntity.ok(FlightLogResponseDtoMapper.buildFlightLogDataDto(
                flightLogList,
                flightLogRepository.findDutyStatsGroupedByAircraftType(member.getEmail(), startDate, endDate).stream()
                        .collect(Collectors.groupingBy(DutyCountByAircraftType::getAircraftType))
                        .entrySet().stream()
                        .map(entry -> FlightLogResponseDto.DutyByAircraftTypeDto.builder()
                                .aircraftType(entry.getKey())
                                .dutyTotalCount(entry.getValue().stream().mapToLong(DutyCountByAircraftType::getCount).sum())
                                .dutyByAircraftType(
                                        entry.getValue().stream()
                                                .map(d -> new FlightLogResponseDto.DutyDto(d.getDuty() != null ? d.getDuty() : "UNKNOWN", d.getCount()))
                                                .toList()
                                )
                                .build()
                        )
                        .toList()
        ));
    }
}
