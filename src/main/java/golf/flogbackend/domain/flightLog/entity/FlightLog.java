package golf.flogbackend.domain.flightLog.entity;

import golf.flogbackend.util.Timestamped;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.TimeZone;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class FlightLog extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String memberId;

    @Column
    private String flightId;
    @Column
    private LocalDate flightDate;
    @Column
    private String airline;
    @Column
    private String duty;

    //aircraft info
    @Column
    private String aircraftNumber;
    @Column
    private String aircraftType;


    //departure info
    @Column
    private String depAirportCode;
    @Column
    private String depAirportName;
    @Column
    private String depCountryCode;
    @Column
    private String depCountryName;
    @Column
    private Double depAirportLocationLon;
    @Column
    private Double depAirportLocationLat;
    @Column
    private TimeZone depAirportTimezone;
    @Column
    private ZonedDateTime depScheduledTimeUtc;
    @Column
    private ZonedDateTime depScheduledTimeLocal;
    @Column
    private ZonedDateTime depRevisedTimeUtc;
    @Column
    private ZonedDateTime depRevisedTimeLocal;
    @Column
    private ZonedDateTime depRunwayTimeUtc;
    @Column
    private ZonedDateTime depRunwayTimeLocal;


    //arrival info
    @Column
    private String arrivalAirportCode;
    @Column
    private String arrivalAirportName;
    @Column
    private String arrivalCountryCode;
    @Column
    private String arrivalCountryName;
    @Column
    private Double arrivalAirportLocationLon;
    @Column
    private Double arrivalAirportLocationLat;
    @Column
    private TimeZone arrivalAirportTimezone;
    @Column
    private ZonedDateTime arrivalScheduledTimeUtc;
    @Column
    private ZonedDateTime arrivalScheduledTimeLocal;
    @Column
    private ZonedDateTime arrivalPredictedTimeUtc;
    @Column
    private ZonedDateTime arrivalPredictedTimeLocal;


    //distance info
    @Column
    private Double distanceKilometers;
    @Column
    private Double distanceMiles;
    @Column
    private Double distanceMeters;


    @Column
    private Long flightTime;
}
