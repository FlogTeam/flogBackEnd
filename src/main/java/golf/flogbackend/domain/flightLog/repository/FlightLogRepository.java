package golf.flogbackend.domain.flightLog.repository;

import golf.flogbackend.domain.flightLog.entity.FlightLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface FlightLogRepository extends JpaRepository<FlightLog, Long> {
    Optional<FlightLog> findByMemberIdAndFlightIdAndFlightDate(String memberId, String flightId, LocalDate flightDate);
}
