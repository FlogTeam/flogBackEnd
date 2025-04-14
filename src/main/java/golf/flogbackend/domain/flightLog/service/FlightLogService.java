package golf.flogbackend.domain.flightLog.service;

import golf.flogbackend.domain.airport.entity.Airport;
import golf.flogbackend.domain.airport.repository.AirportRepository;
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
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.StringUtils;

import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto.FlightLogAllInfoDto;
import static golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto.FlightLogSaveResponseDto;
import static golf.flogbackend.domain.flightLog.support.FlightLogResponseDtoMapper.*;
import static golf.flogbackend.domain.flightLog.support.FlightLogUtil.distanceInKilometerByHaversine;


@Service
@RequiredArgsConstructor
public class FlightLogService {
    private final AirportRepository airportRepository;
    @Value("${api.key}")
    private String apiKey;
    private final FlightLogRepository flightLogRepository;
    private final CrewRepository crewRepository;
    private final FlightLogUtil flightLogUtil;

    @Transactional
    public ResponseEntity<FlightLogSaveResponseDto> saveFlightLogStepOne(Member member, SaveFlightLogRequestDto saveFlightLogRequestDto) throws ParseException {
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

        ResponseEntity<String> flightData = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        String body = flightData.getBody().substring(1, flightData.getBody().length() - 1);

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


        ZonedDateTime depRunwayLocal = ZonedDateTime.parse((String) ((JSONObject) departureData.get("runwayTime")).get("local"), formatter);
        ZonedDateTime depRunwayUtc = ZonedDateTime.parse((String) ((JSONObject) departureData.get("runwayTime")).get("utc"), formatter);
        ZonedDateTime arrivalPredictedLocal = ZonedDateTime.parse((String) ((JSONObject) arrivalData.get("predictedTime")).get("local"), formatter);
        ZonedDateTime arrivalPredictedUtc = ZonedDateTime.parse((String) ((JSONObject) arrivalData.get("predictedTime")).get("utc"), formatter);

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
                        .dateUtc(depRunwayUtc.toLocalDate())
                        .dateLocal(depRunwayLocal.toLocalDate())
                        .scheduledTimeUtc(ZonedDateTime.parse((String) ((JSONObject) departureData.get("scheduledTime")).get("utc"), formatter).toLocalTime())
                        .scheduledTimeLocal(ZonedDateTime.parse((String) ((JSONObject) departureData.get("scheduledTime")).get("local"), formatter).toLocalTime())
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
                        .dateUtc(arrivalPredictedUtc.toLocalDate())
                        .dateLocal(arrivalPredictedLocal.toLocalDate())
                        .scheduledTimeUtc(ZonedDateTime.parse((String) ((JSONObject) arrivalData.get("scheduledTime")).get("utc"), formatter).toLocalTime())
                        .scheduledTimeLocal(ZonedDateTime.parse((String) ((JSONObject) arrivalData.get("scheduledTime")).get("local"), formatter).toLocalTime())
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

        LocalDate depDateUtc = updateFlightLogRequestDto.getDepDateUtc();
        LocalDate depDateLocal = updateFlightLogRequestDto.getDepDateLocal();
        LocalDate arrivalDateUtc = updateFlightLogRequestDto.getArrivalDateUtc();
        LocalDate arrivalDateLocal = updateFlightLogRequestDto.getArrivalDateLocal();
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
                .dateUtc(depDateUtc == null ? departure.getDateUtc() : depDateUtc)
                .dateLocal(depDateLocal == null ? departure.getDateLocal() : depDateLocal)
                .scheduledTimeUtc(depScheduledTimeUtc)
                .scheduledTimeLocal(depScheduledTimeLocal)
                .actualTimeUtc(depActualTimeUtc)
                .actualTimeLocal(depActualTimeLocal)
                .build();

        arrival = arrival.toBuilder()
                .dateUtc(arrivalDateUtc == null ? arrival.getDateUtc() : arrivalDateUtc)
                .dateLocal(arrivalDateLocal == null ? arrival.getDateLocal() : arrivalDateLocal)
                .scheduledTimeUtc(arrivalScheduledTimeUtc)
                .scheduledTimeLocal(arrivalScheduledTimeLocal)
                .actualTimeUtc(arrivalActualTimeUtc)
                .actualTimeLocal(arrivalActualTimeLocal)
                .build();

        if (!StringUtils.isEmptyOrWhitespace(setDepAirportCode)) {
            if (setDepAirportCode.equals(setArrivalAirportCode))
                throw new IllegalArgumentException("DepAirportCode and ArrivalAirportCode cannot be the same");
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

        if ((!StringUtils.isEmptyOrWhitespace(setDepAirportCode) && !setDepAirportCode.equals(getDepAirportCode)) ||
                (!StringUtils.isEmptyOrWhitespace(setArrivalAirportCode) && !setArrivalAirportCode.equals(getArrivalAirportCode))) {
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
                .flightTime(Duration.between(LocalDateTime.of(flightLog.getDeparture().getDateUtc(), depActualTimeUtc),
                        LocalDateTime.of(flightLog.getDeparture().getDateUtc(), arrivalActualTimeUtc)).toSeconds())
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
