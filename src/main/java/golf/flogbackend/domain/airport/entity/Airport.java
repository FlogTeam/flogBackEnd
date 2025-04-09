package golf.flogbackend.domain.airport.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.TimeZone;

@Entity
@Getter
public class Airport {
    @Id
    private String code;

    @Column
    private String countryCode;
    @Column
    private String cityCode;
    @Column
    private String name;
    @Column
    private Double lon;
    @Column
    private Double lat;
    @Column @Setter
    private TimeZone timeZone;
}
