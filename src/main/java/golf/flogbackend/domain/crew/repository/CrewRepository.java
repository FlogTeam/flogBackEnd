package golf.flogbackend.domain.crew.repository;

import golf.flogbackend.domain.crew.entity.Crew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrewRepository  extends JpaRepository<Crew, Long> {
}
