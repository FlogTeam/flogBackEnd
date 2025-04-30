package golf.flogbackend.domain.nickname.repository;

import golf.flogbackend.domain.nickname.entity.Adjective;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdjectiveRepository extends JpaRepository<Adjective, Long> {
}
