package golf.flogbackend.domain.flightLog.service;

import golf.flogbackend.domain.flightLog.dto.FlightLogSummaryResponseDto;
import golf.flogbackend.domain.flightLog.repository.FlightLogRepository;
import golf.flogbackend.domain.flightLog.support.FlightLogResponseDtoMapper;
import golf.flogbackend.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightLogGetService {
    private final FlightLogRepository flightLogRepository;

    public ResponseEntity<List<FlightLogSummaryResponseDto>> getFlightLogList(Member member) {
        return ResponseEntity.ok(flightLogRepository.findByMemberIdOrderByCreateAtDesc(member.getEmail())
                .stream().map(FlightLogResponseDtoMapper::buildFlightLogSummaryResponseDto)
                .toList());
    }
}
