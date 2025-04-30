package golf.flogbackend.domain.nickname.repository;

import golf.flogbackend.domain.nickname.entity.Noun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NounRepository extends JpaRepository<Noun, Long> {
}
