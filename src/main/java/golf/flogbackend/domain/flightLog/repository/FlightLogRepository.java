package golf.flogbackend.domain.flightLog.repository;

import golf.flogbackend.domain.flightLog.entity.FlightLog;
import golf.flogbackend.domain.flightLog.support.DutyCountByAircraftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightLogRepository extends JpaRepository<FlightLog, Long> {
    Optional<FlightLog> findByMemberIdAndFlightIdAndFlightDate(String memberId, String flightId, LocalDate flightDate);

    List<FlightLog> findByMemberIdOrderByCreateAtDesc(String memberId);

    List<FlightLog> findByMemberIdAndFlightDateBetween(String memberId, LocalDate flightDateAfter, LocalDate flightDateBefore);

    @Query("""
                SELECT 
                    f.aircraft.aircraftType AS aircraftType,
                    UPPER(f.duty) AS duty,
                    COUNT(f) AS count
                FROM FlightLog f
                WHERE f.memberId = :memberId
                AND f.flightDate between :startDate and :endDate
                GROUP BY f.aircraft.aircraftType, f.duty
            """)
    List<DutyCountByAircraftType> findDutyStatsGroupedByAircraftType(@Param("memberId") String memberId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
