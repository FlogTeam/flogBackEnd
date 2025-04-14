package golf.flogbackend.domain.flightLog.service;

import golf.flogbackend.domain.crew.repository.CrewRepository;
import golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto;
import golf.flogbackend.domain.flightLog.dto.FlightLogSummaryResponseDto;
import golf.flogbackend.domain.flightLog.repository.FlightLogRepository;
import golf.flogbackend.domain.flightLog.support.FlightLogResponseDtoMapper;
import golf.flogbackend.domain.member.entity.Member;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
