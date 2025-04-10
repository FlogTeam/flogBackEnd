package golf.flogbackend.domain.flightLog.dto;

import golf.flogbackend.domain.crew.entity.Crew;
import golf.flogbackend.domain.flightLog.entity.Aircraft;
import golf.flogbackend.domain.flightLog.entity.Distance;
import golf.flogbackend.domain.flightLog.entity.FlightLog;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class FlightLogResponseDtoMapper {
    public enum EndpointEnum {
        DEPARTURE,
        ARRIVAL
    }

    public static FlightLogResponseDto.ScheduledTimeDto buildScheduledTimeDto(EndpointEnum endpoint, FlightLog flightLog) {
        LocalTime scheduledTimeUtc = flightLog.getArrival().getScheduledTimeUtc();
        LocalTime scheduledTimeLocal = flightLog.getArrival().getScheduledTimeLocal();
        if (endpoint == EndpointEnum.DEPARTURE) {
            scheduledTimeUtc = flightLog.getDeparture().getScheduledTimeUtc();
            scheduledTimeLocal = flightLog.getDeparture().getScheduledTimeLocal();
        }
        return new FlightLogResponseDto.ScheduledTimeDto(scheduledTimeUtc, scheduledTimeLocal);
    }

    public static FlightLogResponseDto.ActualTimeDto buildActualTimeDto(EndpointEnum endpoint, FlightLog flightLog) {
        LocalTime actualTimeUtc = flightLog.getArrival().getActualTimeUtc();
        LocalTime actualTimeLocal = flightLog.getArrival().getActualTimeLocal();
        if (endpoint == EndpointEnum.DEPARTURE) {
            actualTimeUtc = flightLog.getDeparture().getActualTimeUtc();
            actualTimeLocal = flightLog.getDeparture().getActualTimeLocal();
        }
        return new FlightLogResponseDto.ActualTimeDto(actualTimeUtc, actualTimeLocal);
    }

    public static FlightLogResponseDto.DateInfoDto buildDateInfoDto(EndpointEnum endpoint, FlightLog flightLog) {
        LocalDate dateUtc = flightLog.getArrival().getDateUtc();
        LocalDate dateLocal = flightLog.getArrival().getDateLocal();
        if (endpoint == EndpointEnum.DEPARTURE) {
            dateUtc = flightLog.getDeparture().getDateUtc();
            dateLocal = flightLog.getDeparture().getDateLocal();
        }
        return new FlightLogResponseDto.DateInfoDto(dateUtc, dateLocal);
    }

    public static FlightLogResponseDto.AirportDto buildAirportDto(EndpointEnum endpoint, FlightLog flightLog) {
        String airportCode = flightLog.getArrival().getAirportCode();
        String airportName = flightLog.getArrival().getAirportName();
        if (endpoint == EndpointEnum.DEPARTURE) {
            airportCode = flightLog.getDeparture().getAirportCode();
            airportName = flightLog.getDeparture().getAirportName();
        }
        return new FlightLogResponseDto.AirportDto(airportCode, airportName);
    }

    public static FlightLogResponseDto.LocationDto buildLocationDto(EndpointEnum endpoint, FlightLog flightLog) {
        Double latitude = flightLog.getArrival().getAirportLocationLat();
        Double longitude = flightLog.getArrival().getAirportLocationLon();
        if (endpoint == EndpointEnum.DEPARTURE) {
            latitude = flightLog.getDeparture().getAirportLocationLat();
            longitude = flightLog.getDeparture().getAirportLocationLon();
        }
        return new FlightLogResponseDto.LocationDto(latitude, longitude);
    }

    public static FlightLogResponseDto.CountryDto buildCountryDto(EndpointEnum endpoint, FlightLog flightLog) {
        String countryCode = flightLog.getArrival().getCountryCode();
        String countryName = flightLog.getArrival().getCountryName();
        if (endpoint == EndpointEnum.DEPARTURE) {
            countryCode = flightLog.getDeparture().getCountryCode();
            countryName = flightLog.getDeparture().getCountryName();
        }
        return new FlightLogResponseDto.CountryDto(countryCode, countryName);
    }

    public static FlightLogResponseDto.FlightInfoDto buildFlightInfoDto(FlightLog flightLog) {
        return new FlightLogResponseDto.FlightInfoDto(flightLog.getFlightId(), flightLog.getAirline());
    }

    public static FlightLogResponseDto.AircraftDto buildAircraftDto(Aircraft aircraft) {
        return new FlightLogResponseDto.AircraftDto(aircraft.getAircraftNumber(), aircraft.getAircraftType());
    }

    public static FlightLogResponseDto.DistanceDto buildDistanceDto(Distance distance) {
        return FlightLogResponseDto.DistanceDto.builder()
                .distanceKilometers(distance.getKilometers())
                .distanceMeters(distance.getMeters())
                .distanceMiles(distance.getMiles())
                .build();
    }

    public static FlightLogResponseDto.EtcInfoDto buildEtcInfoDto(FlightLog flightLog, List<Crew> crewList) {
        return FlightLogResponseDto.EtcInfoDto.builder()
                .duty(flightLog.getDuty())
                .crewMembers(crewList.stream()
                        .map(c -> new FlightLogResponseDto.CrewDto(c.getName())).toList())
                .build();
    }

    public static FlightLogResponseDto.DepartureDto buildDepartureDto(FlightLog flightLog) {
        return FlightLogResponseDto.DepartureDto.builder()
                .dateInfo(buildDateInfoDto(EndpointEnum.DEPARTURE, flightLog))
                .airport(buildAirportDto(EndpointEnum.DEPARTURE, flightLog))
                .country(buildCountryDto(EndpointEnum.DEPARTURE, flightLog))
                .location(buildLocationDto(EndpointEnum.DEPARTURE, flightLog))
                .timeZone(flightLog.getDeparture().getAirportTimezone())
                .scheduledTime(buildScheduledTimeDto(EndpointEnum.DEPARTURE, flightLog))
                .actualTime(buildActualTimeDto(EndpointEnum.DEPARTURE, flightLog))
                .build();
    }

    public static FlightLogResponseDto.ArrivalDto buildArrivalDto(FlightLog flightLog) {
        return FlightLogResponseDto.ArrivalDto.builder()
                .dateInfo(buildDateInfoDto(EndpointEnum.ARRIVAL, flightLog))
                .airport(buildAirportDto(EndpointEnum.ARRIVAL, flightLog))
                .country(buildCountryDto(EndpointEnum.ARRIVAL, flightLog))
                .location(buildLocationDto(EndpointEnum.ARRIVAL, flightLog))
                .timeZone(flightLog.getArrival().getAirportTimezone())
                .scheduledTime(buildScheduledTimeDto(EndpointEnum.ARRIVAL, flightLog))
                .actualTime(buildActualTimeDto(EndpointEnum.ARRIVAL, flightLog))
                .build();
    }

    public static FlightLogResponseDto.FlightLogAllInfoDto buildAggregateDto(FlightLog flightLog, List<Crew> crewList) {
        return FlightLogResponseDto.FlightLogAllInfoDto.builder()
                .flightLogId(flightLog.getId())
                .memberId(flightLog.getMemberId())
                .flightInfo(buildFlightInfoDto(flightLog))
                .departure(buildDepartureDto(flightLog))
                .arrival(buildArrivalDto(flightLog))
                .aircraft(buildAircraftDto(flightLog.getAircraft()))
                .distance(buildDistanceDto(flightLog.getDistance()))
                .flightTime(new FlightLogResponseDto.FlightTimeDto(flightLog.getFlightTime()))
                .etcInfo(buildEtcInfoDto(flightLog, crewList))
                .build();
    }
}
