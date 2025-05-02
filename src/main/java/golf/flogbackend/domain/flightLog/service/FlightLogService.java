package golf.flogbackend.domain.flightLog.service;

import golf.flogbackend.domain.airport.entity.Airport;
import golf.flogbackend.domain.country.entity.Country;
import golf.flogbackend.domain.crew.entity.Crew;
import golf.flogbackend.domain.crew.repository.CrewRepository;
import golf.flogbackend.domain.flightLog.dto.FlightLogDtoBase;
import golf.flogbackend.domain.flightLog.dto.FlightLogRequestDto;
import golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto;
import golf.flogbackend.domain.flightLog.dto.UpdateFlightLogRequestDto;
import golf.flogbackend.domain.flightLog.entity.*;
import golf.flogbackend.domain.flightLog.repository.FlightLogRepository;
import golf.flogbackend.domain.flightLog.support.FlightLogUtil;
import golf.flogbackend.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import static golf.flogbackend.domain.flightLog.support.FlightLogResponseDtoMapper.buildFlightLogAllInfoDto;
import static golf.flogbackend.domain.flightLog.support.FlightLogUtil.*;
import static golf.flogbackend.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightLogService {

    private final FlightLogRepository flightLogRepository;
    private final CrewRepository crewRepository;
    private final FlightLogUtil flightLogUtil;

    @Transactional
    public ResponseEntity<FlightLogResponseDto.FlightLogAllInfoDto> createFlightLog(Member member, FlightLogRequestDto flightLogRequestDto) {
        FlightLogDtoBase.FlightInfoDto flightInfo = flightLogRequestDto.getFlightInfo();
        FlightLogDtoBase.DepartureDto departure = flightLogRequestDto.getDeparture();
        FlightLogDtoBase.ArrivalDto arrival = flightLogRequestDto.getArrival();
        FlightLogDtoBase.AircraftDto aircraft = flightLogRequestDto.getAircraft();
        FlightLogDtoBase.DistanceDto distance = flightLogRequestDto.getDistance();

        return ResponseEntity.ok(buildFlightLogAllInfoDto(flightLogRepository
                .save(FlightLog.builder()
                        .memberId(member.getEmail())
                        .flightId(flightInfo.flightId())
                        .flightDate(flightInfo.flightDate())
                        .airline(flightInfo.airline())
                        .departure(Departure.builder()
                                .dateScheduledUtc(departure.getScheduledDateInfo().utc())
                                .dateScheduledLocal(departure.getScheduledDateInfo().local())
                                .dateActualUtc(departure.getActualDateInfo().utc())
                                .dateActualLocal(departure.getActualDateInfo().local())
                                .scheduledTimeUtc(departure.getScheduledTime().utc())
                                .scheduledTimeLocal(departure.getScheduledTime().local())
                                .actualTimeUtc(departure.getActualTime().utc())
                                .actualTimeLocal(departure.getActualTime().local())
                                .airportLocationLat(departure.getLocation().latitude())
                                .airportLocationLon(departure.getLocation().longitude())
                                .airportCode(departure.getAirport().code())
                                .airportName(departure.getAirport().name())
                                .airportNameKorean(departure.getAirport().nameKorean())
                                .airportTimezone(departure.getTimeZone())
                                .countryCode(departure.getCountry().code())
                                .countryName(departure.getCountry().name())
                                .countryNameKorean(departure.getCountry().nameKorean())
                                .region(departure.getCountry().region())
                                .cityCode(departure.getCity().code())
                                .cityName(departure.getCity().name())
                                .cityNameKorean(departure.getCity().cityNameKorean())
                                .build())
                        .arrival(Arrival.builder()
                                .dateScheduledUtc(arrival.getScheduledDateInfo().utc())
                                .dateScheduledLocal(arrival.getScheduledDateInfo().local())
                                .dateActualUtc(arrival.getActualDateInfo().utc())
                                .dateActualLocal(arrival.getActualDateInfo().local())
                                .scheduledTimeUtc(arrival.getScheduledTime().utc())
                                .scheduledTimeLocal(arrival.getScheduledTime().local())
                                .actualTimeUtc(arrival.getActualTime().utc())
                                .actualTimeLocal(arrival.getActualTime().local())
                                .airportLocationLat(arrival.getLocation().latitude())
                                .airportLocationLon(arrival.getLocation().longitude())
                                .airportCode(arrival.getAirport().code())
                                .airportName(arrival.getAirport().name())
                                .airportNameKorean(arrival.getAirport().nameKorean())
                                .airportTimezone(arrival.getTimeZone())
                                .countryCode(arrival.getCountry().code())
                                .countryName(arrival.getCountry().name())
                                .countryNameKorean(arrival.getCountry().nameKorean())
                                .region(arrival.getCountry().region())
                                .cityCode(arrival.getCity().code())
                                .cityName(arrival.getCity().name())
                                .cityNameKorean(arrival.getCity().cityNameKorean())
                                .build())
                        .aircraft(Aircraft.builder()
                                .aircraftNumber(aircraft.number())
                                .aircraftType(aircraft.type())
                                .build())
                        .distance(Distance.builder()
                                .kilometers(distance.getKilometers())
                                .meters(distance.getMeters())
                                .miles(distance.getMiles())
                                .build())
                        .flightTime(Duration.between(
                                LocalDateTime.of(departure.getScheduledDateInfo().utc(), departure.getScheduledTime().utc()),
                                LocalDateTime.of(arrival.getScheduledDateInfo().utc(), arrival.getScheduledTime().utc())
                        ).toSeconds())
                        .build()), List.of()));
    }

    @Transactional
    public ResponseEntity<FlightLogResponseDto.FlightLogAllInfoDto> updateFlightLog(Member member, UpdateFlightLogRequestDto updateFlightLogRequestDto) {
        Long flightLogId = updateFlightLogRequestDto.getFlightLogId();

        FlightLog flightLog = flightLogUtil.findFlightLogById(flightLogId);

        Aircraft aircraft = flightLog.getAircraft();
        Departure departure = flightLog.getDeparture();
        Arrival arrival = flightLog.getArrival();

        String setDepAirportCode = updateFlightLogRequestDto.getDepAirportCode();
        String getDepAirportCode = flightLog.getDeparture().getAirportCode();

        String setArrivalAirportCode = updateFlightLogRequestDto.getArrivalAirportCode();
        String getArrivalAirportCode = flightLog.getArrival().getAirportCode();

        LocalDate depDateActualUtc = parseDateOrDefault(updateFlightLogRequestDto.getDepDateActualUtc(), departure.getDateActualLocal());
        LocalDate depDateActualLocal = parseDateOrDefault(updateFlightLogRequestDto.getDepDateActualLocal(), departure.getDateActualLocal());

        LocalDate depDateScheduledUtc = parseDateOrDefault(updateFlightLogRequestDto.getDepDateScheduledUtc(), departure.getDateScheduledUtc());
        LocalDate depDateScheduledLocal = parseDateOrDefault(updateFlightLogRequestDto.getDepDateScheduledLocal(), departure.getDateScheduledLocal());

        LocalDate arrivalDateActualUtc = parseDateOrDefault(updateFlightLogRequestDto.getArrivalDateActualUtc(), arrival.getDateActualUtc());
        LocalDate arrivalDateActualLocal = parseDateOrDefault(updateFlightLogRequestDto.getArrivalDateActualLocal(), arrival.getDateActualLocal());

        LocalDate arrivalDateScheduledUtc = parseDateOrDefault(updateFlightLogRequestDto.getArrivalDateScheduledUtc(), arrival.getDateScheduledUtc());
        LocalDate arrivalDateScheduledLocal = parseDateOrDefault(updateFlightLogRequestDto.getArrivalDateScheduledLocal(), arrival.getDateScheduledLocal());

        String airline = updateFlightLogRequestDto.getAirline();
        String aircraftNumber = updateFlightLogRequestDto.getAircraftNumber();
        String aircraftType = updateFlightLogRequestDto.getAircraftType();
        String duty = updateFlightLogRequestDto.getDuty();

        LocalTime depScheduledTimeUtc = parseTimeOrDefault(updateFlightLogRequestDto.getDepScheduledTimeUtc(), departure.getScheduledTimeUtc());
        LocalTime depScheduledTimeLocal = parseTimeOrDefault(updateFlightLogRequestDto.getDepScheduledTimeLocal(), departure.getScheduledTimeLocal());
        LocalTime arrivalScheduledTimeUtc = parseTimeOrDefault(updateFlightLogRequestDto.getArrivalScheduledTimeUtc(), arrival.getScheduledTimeUtc());
        LocalTime arrivalScheduledTimeLocal = parseTimeOrDefault(updateFlightLogRequestDto.getArrivalScheduledTimeLocal(), arrival.getScheduledTimeLocal());

        LocalTime depActualTimeUtc = parseTimeOrDefault(updateFlightLogRequestDto.getDepActualTimeUtc(), departure.getActualTimeUtc());
        LocalTime depActualTimeLocal = parseTimeOrDefault(updateFlightLogRequestDto.getDepActualTimeLocal(), departure.getActualTimeLocal());
        LocalTime arrivalActualTimeUtc = parseTimeOrDefault(updateFlightLogRequestDto.getArrivalActualTimeUtc(), arrival.getActualTimeUtc());
        LocalTime arrivalActualTimeLocal = parseTimeOrDefault(updateFlightLogRequestDto.getArrivalActualTimeLocal(), arrival.getActualTimeLocal());

        LocalDateTime depActualDateTimeUtc = LocalDateTime.of(depDateActualUtc, depActualTimeUtc);
        LocalDateTime arrivalActualDateTimeUtc = LocalDateTime.of(arrivalDateActualUtc, arrivalActualTimeUtc);

        LocalDateTime depActualDateTimeLocal = LocalDateTime.of(depDateActualLocal, depActualTimeLocal);
        LocalDateTime arrivalActualDateTimeLocal = LocalDateTime.of(arrivalDateActualLocal, arrivalActualTimeLocal);

        LocalDateTime depScheduleDateTimeUtc = LocalDateTime.of(depDateScheduledUtc, depScheduledTimeUtc);
        LocalDateTime arrivalScheduleDateTimeUtc = LocalDateTime.of(arrivalDateScheduledUtc, arrivalScheduledTimeUtc);

        LocalDateTime depScheduleDateTimeLocal = LocalDateTime.of(depDateScheduledLocal, depScheduledTimeLocal);
        LocalDateTime arrivalScheduleDateTimeLocal = LocalDateTime.of(arrivalDateScheduledLocal, arrivalScheduledTimeLocal);


        if (arrivalActualDateTimeUtc.isBefore(depActualDateTimeUtc))
            throw new IllegalArgumentException(ARRIVAL_BEFORE_DEPARTURE_ACTUAL_UTC.getCode() + String.format(
                    "도착 시각(Actual/UTC)은 출발 시각(Actual/UTC)보다 빠를 수 없습니다. 출발: %s, 도착: %s",
                    depActualDateTimeUtc, arrivalActualDateTimeUtc
            ));
        if (arrivalActualDateTimeLocal.isBefore(depActualDateTimeLocal))
            throw new IllegalArgumentException(ARRIVAL_BEFORE_DEPARTURE_ACTUAL_LOCAL.getCode() + String.format(
                    "도착 시각(Actual/Local)은 출발 시각(Actual/Local)보다 빠를 수 없습니다. 출발: %s, 도착: %s",
                    depActualDateTimeLocal, arrivalActualDateTimeLocal
            ));

        if (arrivalScheduleDateTimeUtc.isBefore(depScheduleDateTimeUtc))
            throw new IllegalArgumentException(ARRIVAL_BEFORE_DEPARTURE_SCHEDULED_UTC.getCode() + String.format(
                    "도착 시각(Scheduled/UTC)은 출발 시각(Scheduled/UTC)보다 빠를 수 없습니다. 출발: %s, 도착: %s",
                    depScheduleDateTimeUtc, arrivalScheduleDateTimeUtc
            ));

        if (arrivalScheduleDateTimeLocal.isBefore(depScheduleDateTimeLocal))
            throw new IllegalArgumentException(ARRIVAL_BEFORE_DEPARTURE_SCHEDULED_LOCAL.getCode() + String.format(
                    "도착 시각(Scheduled/Local)은 출발 시각(Scheduled/Local)보다 빠를 수 없습니다. 출발: %s, 도착: %s",
                    depScheduleDateTimeLocal, arrivalScheduleDateTimeLocal
            ));

        aircraft = aircraft.toBuilder()
                .aircraftNumber(StringUtils.isEmptyOrWhitespace(aircraftNumber) ? aircraft.getAircraftNumber() : aircraftNumber)
                .aircraftType(StringUtils.isEmptyOrWhitespace(aircraftType) ? aircraft.getAircraftType() : aircraftType)
                .build();

        departure = departure.toBuilder()
                .dateActualUtc(depDateActualUtc)
                .dateActualLocal(depDateActualLocal)
                .dateScheduledUtc(depDateScheduledUtc)
                .dateScheduledLocal(depDateScheduledLocal)
                .scheduledTimeUtc(depScheduledTimeUtc)
                .scheduledTimeLocal(depScheduledTimeLocal)
                .actualTimeUtc(depActualTimeUtc)
                .actualTimeLocal(depActualTimeLocal)
                .build();

        arrival = arrival.toBuilder()
                .dateActualUtc(arrivalDateActualUtc)
                .dateActualLocal(arrivalDateActualLocal)
                .dateScheduledUtc(arrivalDateScheduledUtc)
                .dateScheduledLocal(arrivalDateScheduledLocal)
                .scheduledTimeUtc(arrivalScheduledTimeUtc)
                .scheduledTimeLocal(arrivalScheduledTimeLocal)
                .actualTimeUtc(arrivalActualTimeUtc)
                .actualTimeLocal(arrivalActualTimeLocal)
                .build();


        if (!StringUtils.isEmptyOrWhitespace(setDepAirportCode)) {
            if (setDepAirportCode.equals(setArrivalAirportCode))
                throw new IllegalArgumentException("출발지 공항 코드와 도착지 공항 코드가 같음. 출발지 : " + setDepAirportCode + ", 도착지 : " + setArrivalAirportCode);
            if (!setDepAirportCode.equals(getDepAirportCode)) {
                Airport airport = flightLogUtil.findAirportByCode(setDepAirportCode);
                Country country = flightLogUtil.findCountryByCode(airport.getCountryCode());
                departure = departure.toBuilder()
                        .airportCode(airport.getCode())
                        .airportName(airport.getAirportName())
                        .airportNameKorean(airport.getAirportNameKorean())
                        .cityCode(airport.getCityCode())
                        .cityName(airport.getCityName())
                        .cityNameKorean(airport.getCityNameKorean())
                        .countryCode(airport.getCountryCode())
                        .countryName(country.getCountryName())
                        .countryNameKorean(country.getCountryNameKorean())
                        .region(country.getRegion())
                        .airportLocationLon(airport.getLon())
                        .airportLocationLat(airport.getLat())
                        .airportTimezone(airport.getTimeZone())
                        .build();
            }
        }
        if (!StringUtils.isEmptyOrWhitespace(setArrivalAirportCode) && !setArrivalAirportCode.equals(getArrivalAirportCode)) {
            Airport airport = flightLogUtil.findAirportByCode(setArrivalAirportCode);
            Country country = flightLogUtil.findCountryByCode(airport.getCountryCode());
            arrival = arrival.toBuilder()
                    .airportCode(airport.getCode())
                    .airportName(airport.getAirportName())
                    .airportNameKorean(airport.getAirportNameKorean())
                    .cityCode(airport.getCityCode())
                    .cityName(airport.getCityName())
                    .cityNameKorean(airport.getCityNameKorean())
                    .countryCode(airport.getCountryCode())
                    .countryName(country.getCountryName())
                    .countryNameKorean(country.getCountryNameKorean())
                    .region(country.getRegion())
                    .airportLocationLon(airport.getLon())
                    .airportLocationLat(airport.getLat())
                    .airportTimezone(airport.getTimeZone())
                    .build();
        }

        if ((!StringUtils.isEmptyOrWhitespace(setDepAirportCode) || !StringUtils.isEmptyOrWhitespace(setArrivalAirportCode)) &&
                !(Objects.equals(getDepAirportCode, setDepAirportCode) && Objects.equals(getArrivalAirportCode, setArrivalAirportCode))) {
            Double distanceKilometers = distanceInKilometerByHaversine(
                    departure.getAirportLocationLat(),
                    arrival.getAirportLocationLat(),
                    departure.getAirportLocationLon(),
                    arrival.getAirportLocationLon());
            flightLog = flightLog.toBuilder()
                    .distance(Distance.builder()
                            .kilometers(distanceKilometers)
                            .meters(distanceKilometers * 1000)
                            .miles(distanceKilometers * 0.621371)
                            .build())
                    .build();

        }

        flightLog = flightLog.toBuilder()
                .flightId(flightLog.getFlightId())
                .flightDate(flightLog.getFlightDate())
                .airline(StringUtils.isEmptyOrWhitespace(airline) ? flightLog.getAirline() : airline)
                .duty(StringUtils.isEmptyOrWhitespace(duty) ? flightLog.getDuty() : duty)
                .arrival(arrival)
                .departure(departure)
                .aircraft(aircraft)
                .build();


        flightLog = flightLog.toBuilder()
                .flightTime(Duration.between(depActualDateTimeUtc, arrivalActualDateTimeUtc).toSeconds())
                .build();

        flightLogRepository.save(flightLog);

        return ResponseEntity.ok(buildFlightLogAllInfoDto(flightLog,
                updateFlightLogRequestDto.getCrewMembers() != null ? crewRepository.saveAll(updateFlightLogRequestDto.getCrewMembers()
                        .stream().map(s -> Crew.builder()
                                .name(s)
                                .email(member.getEmail())
                                .flightLogId(flightLogId)
                                .build())
                        .toList()) : List.of()));
    }
}

