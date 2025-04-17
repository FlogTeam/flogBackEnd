package golf.flogbackend.domain.country.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class Country {
    @Id
    private String countryCode;
    @Column
    private String countryName;
    @Column
    private String countryNameKorean;
    @Column
    private String region;
}
