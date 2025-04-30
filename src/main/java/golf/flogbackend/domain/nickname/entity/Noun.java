package golf.flogbackend.domain.nickname.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Noun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String text;
}
