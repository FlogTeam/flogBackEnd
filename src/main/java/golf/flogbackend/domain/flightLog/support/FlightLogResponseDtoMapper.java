package golf.flogbackend.domain.flightLog.support;

import golf.flogbackend.domain.crew.entity.Crew;
import golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto;
import golf.flogbackend.domain.flightLog.dto.FlightLogSummaryResponseDto;
import golf.flogbackend.domain.flightLog.entity.Aircraft;
import golf.flogbackend.domain.flightLog.entity.Distance;
import golf.flogbackend.domain.flightLog.entity.FlightLog;

import java.util.List;
import java.util.Map;

import static golf.flogbackend.domain.flightLog.support.FlightLogUtil.getEndpoint;

public class FlightLogResponseDtoMapper {

    public static FlightLogResponseDto.ScheduledTimeDto buildScheduledTimeDto(EndpointEnum endpoint, FlightLog flightLog) {
        FlightEndpoint flightEndpoint = getEndpoint(flightLog, endpoint);
        return new FlightLogResponseDto.ScheduledTimeDto(flightEndpoint.getScheduledTimeUtc(), flightEndpoint.getScheduledTimeLocal());
    }

    public static FlightLogResponseDto.ActualTimeDto buildActualTimeDto(EndpointEnum endpoint, FlightLog flightLog) {
        FlightEndpoint flightEndpoint = getEndpoint(flightLog, endpoint);
        return new FlightLogResponseDto.ActualTimeDto(flightEndpoint.getActualTimeUtc(), flightEndpoint.getActualTimeLocal());
    }

    public static FlightLogResponseDto.DateInfoDto buildDateInfoDto(EndpointEnum endpoint, FlightLog flightLog) {
        FlightEndpoint flightEndpoint = getEndpoint(flightLog, endpoint);
        return new FlightLogResponseDto.DateInfoDto(flightEndpoint.getDateUtc(), flightEndpoint.getDateLocal());
    }

    public static FlightLogResponseDto.AirportDto buildAirportDto(EndpointEnum endpoint, FlightLog flightLog) {
        FlightEndpoint flightEndpoint = getEndpoint(flightLog, endpoint);
        return new FlightLogResponseDto.AirportDto(flightEndpoint.getAirportCode(), flightEndpoint.getAirportName(),
                flightEndpoint.getAirportNameKorean()
        );
    }

    public static FlightLogResponseDto.CityDto buildCityDto(EndpointEnum endpoint, FlightLog flightLog) {
        FlightEndpoint flightEndpoint = getEndpoint(flightLog, endpoint);
        return new FlightLogResponseDto.CityDto(flightEndpoint.getCityCode(), flightEndpoint.getCityName());
    }

    public static FlightLogResponseDto.LocationDto buildLocationDto(EndpointEnum endpoint, FlightLog flightLog) {
        FlightEndpoint flightEndpoint = getEndpoint(flightLog, endpoint);
        return new FlightLogResponseDto.LocationDto(flightEndpoint.getAirportLocationLat(), flightEndpoint.getAirportLocationLon());
    }

    public static FlightLogResponseDto.CountryDto buildCountryDto(EndpointEnum endpoint, FlightLog flightLog) {
        FlightEndpoint flightEndpoint = getEndpoint(flightLog, endpoint);
        return new FlightLogResponseDto.CountryDto(flightEndpoint.getCountryCode(), flightEndpoint.getCountryName(),
                flightEndpoint.getCountryNameKorean(), flightEndpoint.getRegion());
    }

    public static FlightLogResponseDto.FlightInfoDto buildFlightInfoDto(FlightLog flightLog) {
        return new FlightLogResponseDto.FlightInfoDto(flightLog.getFlightId(), flightLog.getAirline());
    }

    public static FlightLogResponseDto.AircraftDto buildAircraftDto(Aircraft aircraft) {
        return new FlightLogResponseDto.AircraftDto(aircraft.getAircraftNumber(), aircraft.getAircraftType());
    }

    public static FlightLogResponseDto.DistanceDto buildDistanceDto(Distance distance) {
        return FlightLogResponseDto.DistanceDto.builder()
                .kilometers(distance.getKilometers())
                .meters(distance.getMeters())
                .miles(distance.getMiles())
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
                .city(buildCityDto(EndpointEnum.DEPARTURE, flightLog))
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
                .city(buildCityDto(EndpointEnum.ARRIVAL, flightLog))
                .location(buildLocationDto(EndpointEnum.ARRIVAL, flightLog))
                .timeZone(flightLog.getArrival().getAirportTimezone())
                .scheduledTime(buildScheduledTimeDto(EndpointEnum.ARRIVAL, flightLog))
                .actualTime(buildActualTimeDto(EndpointEnum.ARRIVAL, flightLog))
                .build();
    }

    public static FlightLogResponseDto.FlightLogAllInfoDto buildFlightLogAllInfoDto(FlightLog flightLog, List<Crew> crewList) {
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

    public static FlightLogSummaryResponseDto buildFlightLogSummaryResponseDto(FlightLog flightLog) {
        return FlightLogSummaryResponseDto.builder()
                .flightLogId(flightLog.getId())
                .flightId(flightLog.getFlightId())
                .flightDate(flightLog.getFlightDate())
                .flightTime(new FlightLogResponseDto.FlightTimeDto(flightLog.getFlightTime()))
                .airline(flightLog.getAirline())
                .aircraftType(flightLog.getAircraft().getAircraftType())
                .departure(new FlightLogSummaryResponseDto.DepartureDto(
                        flightLog.getDeparture().getAirportCode(),
                        flightLog.getDeparture().getActualTimeLocal()))
                .arrival(new FlightLogSummaryResponseDto.ArrivalDto(
                        flightLog.getArrival().getAirportCode(),
                        flightLog.getArrival().getActualTimeLocal()))
                .build();
    }

    public static FlightLogResponseDto.FlightLogDataDto buildFlightLogDataDto(FlightData stats, Map<String, Map<String, Long>> dutyByAircraftType) {
        return FlightLogResponseDto.FlightLogDataDto.builder()
                .workDays(stats.getWorkDays())
                .totalFlightTime(new FlightLogResponseDto.FlightTimeDto(stats.getTotalFlightTime()))
                .legCount(stats.getLegCount())
                .dhCount(stats.getDhCount())
                .dutyByAircraftType(dutyByAircraftType)
                .build();
    }
}
