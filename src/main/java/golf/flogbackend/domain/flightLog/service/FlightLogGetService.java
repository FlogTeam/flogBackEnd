package golf.flogbackend.domain.flightLog.service;

import golf.flogbackend.domain.crew.repository.CrewRepository;
import golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto;
import golf.flogbackend.domain.flightLog.dto.FlightLogSummaryResponseDto;
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
        startDate = startDate == null ? LocalDate.MIN : startDate;
        endDate = endDate == null ? LocalDate.MAX : endDate;
        if (!startDate.isBefore(endDate)) throw new IllegalArgumentException("startDate must be before endDate");

        return ResponseEntity.ok(FlightLogResponseDtoMapper.buildFlightLogDataDto(
                flightLogRepository.getStatsByMember(member.getEmail(), startDate, endDate),
                flightLogRepository.findDutyStatsGroupedByAircraftType(member.getEmail(), startDate, endDate).stream()
                        .collect(Collectors.groupingBy(
                                d -> d.getAircraftType() != null ? d.getAircraftType() : "UNKNOWN",
                                Collectors.toMap(
                                        d -> d.getDuty() != null ? d.getDuty() : "등록되지 않은 duty",
                                        DutyCountByAircraftType::getCount
                                )
                        ))));
    }
}
