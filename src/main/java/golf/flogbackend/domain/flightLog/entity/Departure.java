package golf.flogbackend.domain.flightLog.entity;

import golf.flogbackend.domain.flightLog.support.FlightEndpoint;
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
public class Departure implements FlightEndpoint {
    @Column
    private LocalDate dateUtc;
    @Column
    private LocalDate dateLocal;
    @Column
    private String airportCode;
    @Column
    private String airportName;
    @Column
    private String airportNameKorean;
    @Column
    private String cityCode;
    @Column
    private String cityName;
    @Column
    private String countryCode;
    @Column
    private String countryName;
    @Column
    private String countryNameKorean;
    @Column
    private String region;
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
