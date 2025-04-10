package golf.flogbackend.domain.flightLog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Distance {
    @Column
    private Double kilometers;
    @Column
    private Double miles;
    @Column
    private Double meters;

}
