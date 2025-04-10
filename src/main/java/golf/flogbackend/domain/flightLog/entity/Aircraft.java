package golf.flogbackend.domain.flightLog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Aircraft {
    @Column
    private String aircraftNumber;
    @Column
    private String aircraftType;
}
