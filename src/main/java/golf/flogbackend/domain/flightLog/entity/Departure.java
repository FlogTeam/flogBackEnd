package golf.flogbackend.domain.flightLog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.TimeZone;

@Embeddable
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Departure {
    @Column
    private LocalDate dateUtc;
    @Column
    private LocalDate dateLocal;
    @Column
    private String airportCode;
    @Column
    private String airportName;
    @Column
    private String countryCode;
    @Column
    private String countryName;
    @Column
    private Double airportLocationLon;
    @Column
    private Double airportLocationLat;
    @Column
    private TimeZone airportTimezone;
    @Column
    private LocalTime scheduledTimeUtc;
    @Column
    private LocalTime scheduledTimeLocal;
    @Column
    private LocalTime actualTimeUtc;
    @Column
    private LocalTime actualTimeLocal;
}
