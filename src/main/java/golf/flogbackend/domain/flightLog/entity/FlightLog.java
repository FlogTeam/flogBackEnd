package golf.flogbackend.domain.flightLog.entity;

import golf.flogbackend.util.Timestamped;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
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
    @Column @Setter
    private String duty;

    //aircraft info
    @Column
    private String aircraftNumber;
    @Column
    private String aircraftType;


    //departure info
    @Column
    private LocalDate depDateUtc;
    @Column
    private LocalDate depDateLocal;
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
    private LocalTime depScheduledTimeUtc;
    @Column
    private LocalTime depScheduledTimeLocal;
    @Column
    private LocalTime depRevisedTimeUtc;
    @Column
    private LocalTime depRevisedTimeLocal;
    @Column
    private LocalTime depRunwayTimeUtc;
    @Column
    private LocalTime depRunwayTimeLocal;


    //arrival info
    @Column
    private LocalDate arrivalDateUtc;
    @Column
    private LocalDate arrivalDateLocal;
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
    private LocalTime arrivalScheduledTimeUtc;
    @Column
    private LocalTime arrivalScheduledTimeLocal;
    @Column
    private LocalTime arrivalPredictedTimeUtc;
    @Column
    private LocalTime arrivalPredictedTimeLocal;


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
