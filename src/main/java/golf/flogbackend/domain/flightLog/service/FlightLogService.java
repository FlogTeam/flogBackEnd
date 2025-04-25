package golf.flogbackend.domain.flightLog.service;

import golf.flogbackend.domain.airport.entity.Airport;
import golf.flogbackend.domain.country.entity.Country;
import golf.flogbackend.domain.crew.entity.Crew;
import golf.flogbackend.domain.crew.repository.CrewRepository;
import golf.flogbackend.domain.flightLog.dto.SaveFlightLogRequestDto;
import golf.flogbackend.domain.flightLog.dto.UpdateFlightLogRequestDto;
import golf.flogbackend.domain.flightLog.entity.*;
import golf.flogbackend.domain.flightLog.repository.FlightLogRepository;
import golf.flogbackend.domain.flightLog.support.FlightLogUtil;
import golf.flogbackend.domain.member.entity.Member;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.StringUtils;

import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

import static golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto.FlightLogAllInfoDto;
import static golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto.FlightLogSaveResponseDto;
import static golf.flogbackend.domain.flightLog.support.FlightLogResponseDtoMapper.*;
import static golf.flogbackend.domain.flightLog.support.FlightLogUtil.distanceInKilometerByHaversine;
import static golf.flogbackend.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightLogService {
    @Value("${api.key}")
    private String apiKey;
    private final FlightLogRepository flightLogRepository;
    private final CrewRepository crewRepository;
    private final FlightLogUtil flightLogUtil;

    @Transactional
    public ResponseEntity<FlightLogSaveResponseDto> saveFlightLog(Member member, SaveFlightLogRequestDto saveFlightLogRequestDto) throws ParseException {
        String flightId = saveFlightLogRequestDto.getFlightId();
        LocalDate flightDate = saveFlightLogRequestDto.getFlightDate();

        Optional<FlightLog> flightLogCheck = flightLogRepository.findByMemberIdAndFlightIdAndFlightDate(member.getEmail(), flightId, flightDate);
        if (flightLogCheck.isPresent())
            throw new EntityExistsException("flightLogId : " + flightLogCheck.get().getId());

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-rapidapi-host", "aerodatabox.p.rapidapi.com");
        headers.add("x-rapidapi-key", apiKey);

        StringBuilder apiUrl = new StringBuilder();
        apiUrl.append("https://aerodatabox.p.rapidapi.com/flights/number/")
                .append(flightId)
                .append("/")
                .append(flightDate)
                .append("/?dateLocalRole=Both");


        URI uri = URI.create(apiUrl.toString());

        ResponseEntity<String> flightData;
        String body;
        try {
            flightData = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);
            if (flightData.getBody() != null)
                body = flightData.getBody().substring(1, flightData.getBody().length() - 1);
            else throw new HttpClientErrorException(HttpStatus.NOT_FOUND);

        } catch (HttpClientErrorException b) {
            throw new EntityNotFoundException("검색 실패 : " + flightId + " / " + flightDate);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObj = (JSONObject) jsonParser.parse(body);


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mmXXX");

        JSONObject aircraftData = (JSONObject) jsonObj.get("aircraft");
        JSONObject departureData = (JSONObject) jsonObj.get("departure");
        JSONObject depAirportData = (JSONObject) departureData.get("airport");

        JSONObject arrivalData = (JSONObject) jsonObj.get("arrival");
        JSONObject arrivalAirportData = (JSONObject) arrivalData.get("airport");

        JSONObject greatCircleDistanceData = (JSONObject) jsonObj.get("greatCircleDistance");

        String depCountryCode = (String) depAirportData.get("countryCode");
        String arrivalCountryCode = (String) arrivalAirportData.get("countryCode");


        ZonedDateTime depScheduleLocal = ZonedDateTime.parse((String) ((JSONObject) departureData.get("scheduledTime")).get("local"), formatter);
        ZonedDateTime depScheduleUtc = ZonedDateTime.parse((String) ((JSONObject) departureData.get("scheduledTime")).get("utc"), formatter);
        ZonedDateTime arrivalScheduleUtc = ZonedDateTime.parse((String) ((JSONObject) arrivalData.get("scheduledTime")).get("utc"), formatter);
        ZonedDateTime arrivalScheduleLocal = ZonedDateTime.parse((String) ((JSONObject) arrivalData.get("scheduledTime")).get("local"), formatter);

        LocalTime depScheduleTimeUtc = depScheduleUtc.toLocalTime();
        LocalTime depScheduleTimeLocal = depScheduleLocal.toLocalTime();

        LocalTime arrivalScheduleTimeUtc = arrivalScheduleUtc.toLocalTime();
        LocalTime arrivalScheduleTimeLocal = arrivalScheduleLocal.toLocalTime();

        Airport depAirport = flightLogUtil.findAirportByCode((String) depAirportData.get("iata"));
        Airport arriAirport = flightLogUtil.findAirportByCode((String) arrivalAirportData.get("iata"));
        Country depCountry = flightLogUtil.findCountryByCode(depCountryCode);
        Country arriCountry = flightLogUtil.findCountryByCode(arrivalCountryCode);

        FlightLog flightLog = flightLogRepository.save(FlightLog.builder()
                .flightId(saveFlightLogRequestDto.getFlightId())
                .flightDate(flightDate)
                .airline((String) ((JSONObject) jsonObj.get("airline")).get("name"))
                .memberId(member.getEmail())
                .departure(Departure.builder()
                        .dateScheduledUtc(depScheduleUtc.toLocalDate())
                        .dateScheduledLocal(depScheduleLocal.toLocalDate())
                        .dateActualUtc(depScheduleUtc.toLocalDate())
                        .dateActualLocal(depScheduleLocal.toLocalDate())
                        .scheduledTimeUtc(depScheduleTimeUtc)
                        .scheduledTimeLocal(depScheduleTimeLocal)
                        .actualTimeUtc(depScheduleTimeUtc)
                        .actualTimeLocal(depScheduleTimeLocal)
                        .airportLocationLat((Double) ((JSONObject) depAirportData.get("location")).get("lat"))
                        .airportLocationLon((Double) ((JSONObject) depAirportData.get("location")).get("lon"))
                        .airportCode(depAirport.getCode())
                        .airportName(depAirport.getAirportName())
                        .airportNameKorean(depAirport.getAirportNameKorean())
                        .airportTimezone(TimeZone.getTimeZone((String) depAirportData.get("timeZone")))
                        .countryCode(depCountryCode)
                        .countryName(depCountry.getCountryName())
                        .countryNameKorean(depCountry.getCountryNameKorean())
                        .region(depCountry.getRegion())
                        .cityCode(depAirport.getCityCode())
                        .cityName(depAirport.getCityName())
                        .build())
                .arrival(Arrival.builder()
                        .dateScheduledUtc(arrivalScheduleUtc.toLocalDate())
                        .dateScheduledLocal(arrivalScheduleLocal.toLocalDate())
                        .dateActualUtc(arrivalScheduleUtc.toLocalDate())
                        .dateActualLocal(arrivalScheduleLocal.toLocalDate())
                        .scheduledTimeUtc(arrivalScheduleTimeUtc)
                        .scheduledTimeLocal(arrivalScheduleTimeLocal)
                        .actualTimeUtc(arrivalScheduleTimeUtc)
                        .actualTimeLocal(arrivalScheduleTimeLocal)
                        .airportLocationLat((Double) ((JSONObject) arrivalAirportData.get("location")).get("lat"))
                        .airportLocationLon((Double) ((JSONObject) arrivalAirportData.get("location")).get("lon"))
                        .airportCode(arriAirport.getCode())
                        .airportName(arriAirport.getAirportName())
                        .airportNameKorean(arriAirport.getAirportNameKorean())
                        .airportTimezone(TimeZone.getTimeZone((String) arrivalAirportData.get("timeZone")))
                        .countryCode(arrivalCountryCode)
                        .countryName(arriCountry.getCountryName())
                        .countryNameKorean(arriCountry.getCountryNameKorean())
                        .region(arriCountry.getRegion())
                        .cityCode(arriAirport.getCityCode())
                        .cityName(arriAirport.getCityName())
                        .build())
                .aircraft(Aircraft.builder()
                        .aircraftNumber((String) aircraftData.get("reg"))
                        .aircraftType((String) aircraftData.get("model"))
                        .build())
                .distance(Distance.builder()
                        .kilometers((Double) greatCircleDistanceData.get("km"))
                        .meters((Double) greatCircleDistanceData.get("meter"))
                        .miles((Double) greatCircleDistanceData.get("mile"))
                        .build())
                .flightTime(Duration.between(depScheduleUtc, arrivalScheduleUtc).toSeconds())
                .build());


        return ResponseEntity.ok(FlightLogSaveResponseDto.builder()
                .flightLogId(flightLog.getId())
                .flightInfo(buildFlightInfoDto(flightLog))
                .departure(buildDepartureDto(flightLog))
                .arrival(buildArrivalDto(flightLog))
                .aircraft(buildAircraftDto(flightLog.getAircraft()))
                .build());
    }

    @Transactional
    public ResponseEntity<FlightLogAllInfoDto> updateFlightLog(Member member, UpdateFlightLogRequestDto updateFlightLogRequestDto) {
        Long flightLogId = updateFlightLogRequestDto.getFlightLogId();

        FlightLog flightLog = flightLogUtil.findFlightLogById(flightLogId);

        Aircraft aircraft = flightLog.getAircraft();
        Departure departure = flightLog.getDeparture();
        Arrival arrival = flightLog.getArrival();

        String setDepAirportCode = updateFlightLogRequestDto.getDepAirportCode();
        String getDepAirportCode = flightLog.getDeparture().getAirportCode();

        String setArrivalAirportCode = updateFlightLogRequestDto.getArrivalAirportCode();
        String getArrivalAirportCode = flightLog.getArrival().getAirportCode();

        LocalDate depDateActualUtc = updateFlightLogRequestDto.getDepDateActualUtc();
        LocalDate depDateActualLocal = updateFlightLogRequestDto.getDepDateActualLocal();

        LocalDate depDateScheduledUtc = updateFlightLogRequestDto.getDepDateScheduledUtc();
        LocalDate depDateScheduledLocal = updateFlightLogRequestDto.getDepDateScheduledLocal();

        LocalDate arrivalDateActualUtc = updateFlightLogRequestDto.getArrivalDateActualUtc();
        LocalDate arrivalDateActualLocal = updateFlightLogRequestDto.getArrivalDateActualLocal();

        LocalDate arrivalDateScheduledUtc = updateFlightLogRequestDto.getArrivalDateScheduledLocal();
        LocalDate arrivalDateScheduledLocal = updateFlightLogRequestDto.getArrivalDateScheduledLocal();

        String airline = updateFlightLogRequestDto.getAirline();
        String aircraftNumber = updateFlightLogRequestDto.getAircraftNumber();
        String aircraftType = updateFlightLogRequestDto.getAircraftType();
        String duty = updateFlightLogRequestDto.getDuty();

        LocalTime depScheduledTimeUtc = updateFlightLogRequestDto.getDepScheduledTimeUtc() == null ? departure.getScheduledTimeUtc() : updateFlightLogRequestDto.getDepScheduledTimeUtc();
        LocalTime depScheduledTimeLocal = updateFlightLogRequestDto.getDepScheduledTimeLocal() == null ? departure.getScheduledTimeLocal() : updateFlightLogRequestDto.getDepScheduledTimeLocal();
        LocalTime arrivalScheduledTimeUtc = updateFlightLogRequestDto.getArrivalScheduledTimeUtc() == null ? arrival.getScheduledTimeUtc() : updateFlightLogRequestDto.getArrivalScheduledTimeUtc();
        LocalTime arrivalScheduledTimeLocal = updateFlightLogRequestDto.getArrivalScheduledTimeLocal() == null ? arrival.getScheduledTimeLocal() : updateFlightLogRequestDto.getArrivalScheduledTimeLocal();

        LocalTime depActualTimeUtc = updateFlightLogRequestDto.getDepActualTimeUtc() == null ? depScheduledTimeUtc : updateFlightLogRequestDto.getDepActualTimeUtc();
        LocalTime depActualTimeLocal = updateFlightLogRequestDto.getDepActualTimeLocal() == null ? depScheduledTimeLocal : updateFlightLogRequestDto.getDepActualTimeLocal();
        LocalTime arrivalActualTimeUtc = updateFlightLogRequestDto.getArrivalActualTimeUtc() == null ? arrivalScheduledTimeUtc : updateFlightLogRequestDto.getArrivalActualTimeUtc();
        LocalTime arrivalActualTimeLocal = updateFlightLogRequestDto.getArrivalActualTimeLocal() == null ? arrivalScheduledTimeLocal : updateFlightLogRequestDto.getArrivalActualTimeLocal();


        aircraft = aircraft.toBuilder()
                .aircraftNumber(StringUtils.isEmptyOrWhitespace(aircraftNumber) ? aircraft.getAircraftNumber() : aircraftNumber)
                .aircraftType(StringUtils.isEmptyOrWhitespace(aircraftType) ? aircraft.getAircraftType() : aircraftType)
                .build();

        departure = departure.toBuilder()
                .dateActualUtc(depDateActualUtc == null ? departure.getDateActualUtc() : depDateActualUtc)
                .dateActualLocal(depDateActualLocal == null ? departure.getDateActualLocal() : depDateActualLocal)
                .dateScheduledUtc(depDateScheduledUtc == null ? departure.getDateScheduledUtc() : depDateScheduledUtc)
                .dateScheduledLocal(depDateScheduledLocal == null ? departure.getDateScheduledLocal() : depDateScheduledLocal)
                .scheduledTimeUtc(depScheduledTimeUtc)
                .scheduledTimeLocal(depScheduledTimeLocal)
                .actualTimeUtc(depActualTimeUtc)
                .actualTimeLocal(depActualTimeLocal)
                .build();

        arrival = arrival.toBuilder()
                .dateActualUtc(arrivalDateActualUtc == null ? arrival.getDateActualUtc() : arrivalDateActualUtc)
                .dateActualLocal(arrivalDateActualLocal == null ? arrival.getDateActualLocal() : arrivalDateActualLocal)
                .dateScheduledUtc(arrivalDateScheduledUtc == null ? arrival.getDateScheduledUtc() : arrivalDateScheduledUtc)
                .dateScheduledLocal(arrivalDateScheduledLocal == null ?  arrival.getDateScheduledLocal() : arrivalDateScheduledLocal)
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
        LocalDateTime depActualDateTimeUtc = LocalDateTime.of(flightLog.getDeparture().getDateActualUtc(), depActualTimeUtc);
        LocalDateTime arrivalActualDateTimeUtc = LocalDateTime.of(flightLog.getArrival().getDateActualUtc(), arrivalActualTimeUtc);

        LocalDateTime depActualDateTimeLocal = LocalDateTime.of(flightLog.getDeparture().getDateActualUtc(), depActualTimeLocal);
        LocalDateTime arrivalActualDateTimeLocal = LocalDateTime.of(flightLog.getArrival().getDateActualLocal(), arrivalActualTimeLocal);

        LocalDateTime depScheduleDateTimeUtc = LocalDateTime.of(flightLog.getDeparture().getDateScheduledUtc(), depScheduledTimeUtc);
        LocalDateTime arrivalScheduleDateTimeUtc = LocalDateTime.of(flightLog.getArrival().getDateScheduledLocal(), arrivalScheduledTimeUtc);

        LocalDateTime depScheduleDateTimeLocal = LocalDateTime.of(flightLog.getDeparture().getDateScheduledUtc(), depScheduledTimeLocal);
        LocalDateTime arrivalScheduleDateTimeLocal = LocalDateTime.of(flightLog.getArrival().getDateScheduledLocal(), arrivalScheduledTimeLocal);

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

