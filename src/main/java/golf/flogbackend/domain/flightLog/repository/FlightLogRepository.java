package golf.flogbackend.domain.flightLog.repository;

import golf.flogbackend.domain.flightLog.entity.FlightLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightLogRepository extends JpaRepository<FlightLog, Long> {
}
