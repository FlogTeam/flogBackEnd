package golf.flogbackend.domain.crew.repository;

import golf.flogbackend.domain.crew.entity.Crew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrewRepository  extends JpaRepository<Crew, Long> {
    List<Crew> findByFlightLogId(Long flightLogId);
    List<Crew> findByEmail(String email);
}
