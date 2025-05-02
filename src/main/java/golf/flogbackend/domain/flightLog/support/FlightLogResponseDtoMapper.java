package golf.flogbackend.domain.flightLog.support;

import golf.flogbackend.domain.crew.entity.Crew;
import golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto;
import golf.flogbackend.domain.flightLog.dto.FlightLogSummaryResponseDto;
import golf.flogbackend.domain.flightLog.entity.Aircraft;
import golf.flogbackend.domain.flightLog.entity.Distance;
import golf.flogbackend.domain.flightLog.entity.FlightLog;
import golf.flogbackend.domain.flightLog.enums.Endpoint;
import golf.flogbackend.domain.flightLog.enums.LocationType;

import java.util.List;

import static golf.flogbackend.domain.flightLog.support.FlightLogUtil.getEndpoint;

public class FlightLogResponseDtoMapper {

    public static FlightLogResponseDto.ScheduledTimeDto buildScheduledTimeDto(Endpoint endpoint, FlightLog flightLog) {
        FlightEndpoint flightEndpoint = getEndpoint(flightLog, endpoint);
        return new FlightLogResponseDto.ScheduledTimeDto(flightEndpoint.getScheduledTimeUtc(), flightEndpoint.getScheduledTimeLocal());
    }

    public static FlightLogResponseDto.ActualTimeDto buildActualTimeDto(Endpoint endpoint, FlightLog flightLog) {
        FlightEndpoint flightEndpoint = getEndpoint(flightLog, endpoint);
        return new FlightLogResponseDto.ActualTimeDto(flightEndpoint.getActualTimeUtc(), flightEndpoint.getActualTimeLocal());
    }

    public static FlightLogResponseDto.ScheduledDateInfoDto buildScheduledDateInfoDto(Endpoint endpoint, FlightLog flightLog) {
        FlightEndpoint flightEndpoint = getEndpoint(flightLog, endpoint);
        return new FlightLogResponseDto.ScheduledDateInfoDto(flightEndpoint.getDateScheduledUtc(), flightEndpoint.getDateScheduledLocal());
    }

    public static FlightLogResponseDto.ActualDateInfoDto buildActualDateInfoDto(Endpoint endpoint, FlightLog flightLog) {
        FlightEndpoint flightEndpoint = getEndpoint(flightLog, endpoint);
        return new FlightLogResponseDto.ActualDateInfoDto(flightEndpoint.getDateActualUtc(), flightEndpoint.getDateActualLocal());
    }

    public static FlightLogResponseDto.AirportDto buildAirportDto(Endpoint endpoint, FlightLog flightLog) {
        FlightEndpoint flightEndpoint = getEndpoint(flightLog, endpoint);
        return new FlightLogResponseDto.AirportDto(flightEndpoint.getAirportCode(), flightEndpoint.getAirportName(),
                flightEndpoint.getAirportNameKorean()
        );
    }

    public static FlightLogResponseDto.CityDto buildCityDto(Endpoint endpoint, FlightLog flightLog) {
        FlightEndpoint flightEndpoint = getEndpoint(flightLog, endpoint);
        return new FlightLogResponseDto.CityDto(flightEndpoint.getCityCode(), flightEndpoint.getCityName(), flightEndpoint.getCityNameKorean());
    }

    public static FlightLogResponseDto.LocationDto buildLocationDto(Endpoint endpoint, FlightLog flightLog) {
        FlightEndpoint flightEndpoint = getEndpoint(flightLog, endpoint);
        return new FlightLogResponseDto.LocationDto(flightEndpoint.getAirportLocationLat(), flightEndpoint.getAirportLocationLon());
    }

    public static FlightLogResponseDto.CountryDto buildCountryDto(Endpoint endpoint, FlightLog flightLog) {
        FlightEndpoint flightEndpoint = getEndpoint(flightLog, endpoint);
        return new FlightLogResponseDto.CountryDto(flightEndpoint.getCountryCode(), flightEndpoint.getCountryName(),
                flightEndpoint.getCountryNameKorean(), flightEndpoint.getRegion());
    }

    public static FlightLogResponseDto.FlightInfoDto buildFlightInfoDto(FlightLog flightLog) {
        return new FlightLogResponseDto.FlightInfoDto(flightLog.getFlightId(), flightLog.getAirline(), flightLog.getFlightDate());
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
                .scheduledDateInfo(buildScheduledDateInfoDto(Endpoint.DEPARTURE, flightLog))
                .actualDateInfo(buildActualDateInfoDto(Endpoint.DEPARTURE, flightLog))
                .airport(buildAirportDto(Endpoint.DEPARTURE, flightLog))
                .country(buildCountryDto(Endpoint.DEPARTURE, flightLog))
                .city(buildCityDto(Endpoint.DEPARTURE, flightLog))
                .location(buildLocationDto(Endpoint.DEPARTURE, flightLog))
                .timeZone(flightLog.getDeparture().getAirportTimezone())
                .scheduledTime(buildScheduledTimeDto(Endpoint.DEPARTURE, flightLog))
                .actualTime(buildActualTimeDto(Endpoint.DEPARTURE, flightLog))
                .build();
    }

    public static FlightLogResponseDto.ArrivalDto buildArrivalDto(FlightLog flightLog) {
        return FlightLogResponseDto.ArrivalDto.builder()
                .scheduledDateInfo(buildScheduledDateInfoDto(Endpoint.ARRIVAL, flightLog))
                .actualDateInfo(buildActualDateInfoDto(Endpoint.ARRIVAL, flightLog))
                .airport(buildAirportDto(Endpoint.ARRIVAL, flightLog))
                .country(buildCountryDto(Endpoint.ARRIVAL, flightLog))
                .city(buildCityDto(Endpoint.ARRIVAL, flightLog))
                .location(buildLocationDto(Endpoint.ARRIVAL, flightLog))
                .timeZone(flightLog.getArrival().getAirportTimezone())
                .scheduledTime(buildScheduledTimeDto(Endpoint.ARRIVAL, flightLog))
                .actualTime(buildActualTimeDto(Endpoint.ARRIVAL, flightLog))
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

    public static FlightLogResponseDto.MostVisitedDataDto buildMostVisitedDataDto(List<FlightLog> flightLogList,
                                                                                  LocationType locationType) {
        List<FlightLogResponseDto.VisitedDataDto> mostVisitedDeparture =
                FlightLogUtil.createVisitedDataList(flightLogList,
                        f -> locationType.getClassifier().apply(f).getLeft());
        List<FlightLogResponseDto.VisitedDataDto> mostVisitedArrival =
                FlightLogUtil.createVisitedDataList(flightLogList,
                        f -> locationType.getClassifier().apply(f).getRight());

        return FlightLogResponseDto.MostVisitedDataDto.builder()
                .mostVisitedDeparture(mostVisitedDeparture)
                .mostVisitedArrival(mostVisitedArrival)
                .mostVisitedTotal(FlightLogUtil.concatVisitedDataList(
                        mostVisitedDeparture.stream(), mostVisitedArrival.stream(), flightLogList.size()))
                .build();
    }


    public static FlightLogResponseDto.FlightLogDataDto buildFlightLogDataDto(List<FlightLog> flightLogList,
                                                                              List<FlightLogResponseDto.DutyByAircraftTypeDto> dutyByAircraftType,
                                                                              List<FlightLogResponseDto.CrewMateDto> crewList) {
        return FlightLogResponseDto.FlightLogDataDto.builder()
                .workDays(flightLogList.stream().map(FlightLog::getFlightDate).distinct().count())
                .totalFlightTime(new FlightLogResponseDto.FlightTimeDto(flightLogList.stream().map(FlightLog::getFlightTime).reduce(Long::sum).orElse(0L)))
                .legCount((long) flightLogList.size())
                .dhCount(flightLogList.stream().map(FlightLog::getDuty).filter(d -> d != null && (d.equals("DH") || d.equals("dh"))).count())
                .mostVisitedAirport(buildMostVisitedDataDto(flightLogList, LocationType.AIRPORT))
                .mostVisitedCities(buildMostVisitedDataDto(flightLogList, LocationType.CITY))
                .mostVisitedCountries(buildMostVisitedDataDto(flightLogList, LocationType.COUNTRY))
                .crewMate(crewList)
                .dutyByAircraftType(dutyByAircraftType)
                .build();
    }
}
