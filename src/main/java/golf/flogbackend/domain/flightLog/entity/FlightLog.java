package golf.flogbackend.domain.flightLog.entity;

import golf.flogbackend.util.Timestamped;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@DynamicUpdate
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

    @Embedded
    private Aircraft aircraft;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "dateUtc", column = @Column(name = "departure_date_utc")),
            @AttributeOverride(name = "dateLocal", column = @Column(name = "departure_date_local")),
            @AttributeOverride(name = "airportCode", column = @Column(name = "departure_airport_code")),
            @AttributeOverride(name = "airportName", column = @Column(name = "departure_airport_name")),
            @AttributeOverride(name = "airportNameKorean", column = @Column(name = "departure_airport_name_korean")),
            @AttributeOverride(name = "countryCode", column = @Column(name = "departure_country_code")),
            @AttributeOverride(name = "countryName", column = @Column(name = "departure_country_name")),
            @AttributeOverride(name = "countryNameKorean", column = @Column(name = "departure_country_name_korean")),
            @AttributeOverride(name = "region", column = @Column(name = "departure_region")),
            @AttributeOverride(name = "cityCode", column = @Column(name = "departure_city_code")),
            @AttributeOverride(name = "cityName", column = @Column(name = "departure_city_name")),
            @AttributeOverride(name = "airportLocationLon", column = @Column(name = "departure_airport_location_lon")),
            @AttributeOverride(name = "airportLocationLat", column = @Column(name = "departure_airport_location_lat")),
            @AttributeOverride(name = "airportTimezone", column = @Column(name = "departure_airport_timezone")),
            @AttributeOverride(name = "scheduledTimeUtc", column = @Column(name = "departure_scheduled_time_utc")),
            @AttributeOverride(name = "scheduledTimeLocal", column = @Column(name = "departure_scheduled_time_local")),
            @AttributeOverride(name = "actualTimeUtc", column = @Column(name = "departure_actual_time_utc")),
            @AttributeOverride(name = "actualTimeLocal", column = @Column(name = "departure_actual_time_local"))
    })
    private Departure departure;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "dateUtc", column = @Column(name = "arrival_date_utc")),
            @AttributeOverride(name = "dateLocal", column = @Column(name = "arrival_date_local")),
            @AttributeOverride(name = "airportCode", column = @Column(name = "arrival_airport_code")),
            @AttributeOverride(name = "airportName", column = @Column(name = "arrival_airport_name")),
            @AttributeOverride(name = "airportNameKorean", column = @Column(name = "arrival_airport_name_korean")),
            @AttributeOverride(name = "countryCode", column = @Column(name = "arrival_country_code")),
            @AttributeOverride(name = "countryName", column = @Column(name = "arrival_country_name")),
            @AttributeOverride(name = "countryNameKorean", column = @Column(name = "arrival_country_name_korean")),
            @AttributeOverride(name = "region", column = @Column(name = "arrival_region")),
            @AttributeOverride(name = "cityCode", column = @Column(name = "arrival_city_code")),
            @AttributeOverride(name = "cityName", column = @Column(name = "arrival_city_name")),
            @AttributeOverride(name = "airportLocationLon", column = @Column(name = "arrival_airport_location_lon")),
            @AttributeOverride(name = "airportLocationLat", column = @Column(name = "arrival_airport_location_lat")),
            @AttributeOverride(name = "airportTimezone", column = @Column(name = "arrival_airport_timezone")),
            @AttributeOverride(name = "scheduledTimeUtc", column = @Column(name = "arrival_scheduled_time_utc")),
            @AttributeOverride(name = "scheduledTimeLocal", column = @Column(name = "arrival_scheduled_time_local")),
            @AttributeOverride(name = "actualTimeUtc", column = @Column(name = "arrival_actual_time_utc")),
            @AttributeOverride(name = "actualTimeLocal", column = @Column(name = "arrival_actual_time_local"))
    })
    private Arrival arrival;

    @Embedded
    private Distance distance;

    @Column
    private Long flightTime;
}
