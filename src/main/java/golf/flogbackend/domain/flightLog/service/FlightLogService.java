package golf.flogbackend.domain.flightLog.service;

import golf.flogbackend.domain.airport.entity.Airport;
import golf.flogbackend.domain.airport.repository.AirportRepository;
import golf.flogbackend.domain.country.repository.CountryRepository;
import golf.flogbackend.domain.crew.entity.Crew;
import golf.flogbackend.domain.crew.repository.CrewRepository;
import golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto;
import golf.flogbackend.domain.flightLog.dto.SaveFlightLogRequestDto;
import golf.flogbackend.domain.flightLog.dto.UpdateFlightLogRequestDto;
import golf.flogbackend.domain.flightLog.entity.FlightLog;
import golf.flogbackend.domain.flightLog.repository.FlightLogRepository;
import golf.flogbackend.domain.member.entity.Member;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;


@Service
@RequiredArgsConstructor
public class FlightLogService {
    @Value("${api.key}")
    private String apiKey;
    private final FlightLogRepository flightLogRepository;
    private final CountryRepository countryRepository;
    private final CrewRepository crewRepository;
    private final AirportRepository airportRepository;

    @Transactional
    public ResponseEntity<FlightLogResponseDto.StepOneResponseDto> saveFlightLogStepOne(Member member, SaveFlightLogRequestDto saveFlightLogRequestDto) throws ParseException {
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
        ZonedDateTime arrivalPredictedLocal = ZonedDateTime.parse((String) ((JSONObject) arrivalData.get("predictedTime")).get("local"), formatter);

        FlightLog flightLog = flightLogRepository.save(FlightLog.builder()
                .flightId(saveFlightLogRequestDto.getFlightId())
                .flightDate(flightDate)
                .depDateLocal(depRunwayLocal.toLocalDate())
                .arrivalDateLocal(arrivalPredictedLocal.toLocalDate())
                .airline((String) ((JSONObject) jsonObj.get("airline")).get("name"))
                .memberId(member.getEmail())
                .aircraftNumber((String) aircraftData.get("reg"))
                .aircraftType((String) aircraftData.get("model"))
                .depAirportCode((String) depAirportData.get("iata"))
                .depAirportName((String) depAirportData.get("name"))
                .depCountryCode(depCountryCode)
                .depCountryName(countryRepository.findById(depCountryCode).orElseThrow(
                                EntityNotFoundException::new)
                        .getCountryName())
                .depAirportLocationLat((Double) ((JSONObject) depAirportData.get("location")).get("lat"))
                .depAirportLocationLon((Double) ((JSONObject) depAirportData.get("location")).get("lon"))
                .depAirportTimezone(TimeZone.getTimeZone((String) depAirportData.get("timeZone")))
                .depScheduledTimeLocal(ZonedDateTime.parse((String) ((JSONObject) departureData.get("scheduledTime")).get("local"), formatter).toLocalTime())
                .depScheduledTimeUtc(ZonedDateTime.parse((String) ((JSONObject) departureData.get("scheduledTime")).get("utc"), formatter).toLocalTime())
                .arrivalAirportCode((String) arrivalAirportData.get("iata"))
                .arrivalAirportName((String) arrivalAirportData.get("name"))
                .arrivalCountryCode(arrivalCountryCode)
                .arrivalCountryName(countryRepository.findById(arrivalCountryCode).orElseThrow(
                                EntityNotFoundException::new)
                        .getCountryName())
                .arrivalAirportLocationLon((Double) ((JSONObject) arrivalAirportData.get("location")).get("lon"))
                .arrivalAirportLocationLat((Double) ((JSONObject) arrivalAirportData.get("location")).get("lat"))
                .arrivalAirportTimezone(TimeZone.getTimeZone((String) arrivalAirportData.get("timeZone")))
                .arrivalScheduledTimeUtc(ZonedDateTime.parse((String) ((JSONObject) arrivalData.get("scheduledTime")).get("utc"), formatter).toLocalTime())
                .arrivalScheduledTimeLocal(ZonedDateTime.parse((String) ((JSONObject) arrivalData.get("scheduledTime")).get("local"), formatter).toLocalTime())
                .distanceKilometers((Double) greatCircleDistanceData.get("km"))
                .distanceMeters((Double) greatCircleDistanceData.get("meter"))
                .distanceMiles((Double) greatCircleDistanceData.get("mile"))
                .build());

        return ResponseEntity.ok(FlightLogResponseDto.StepOneResponseDto.builder()
                .flightLogId(flightLog.getId())
                .flightInfo(FlightLogResponseDto.FlightInfoDto.builder()
                        .flightId(flightLog.getFlightId())
                        .airline(flightLog.getAirline())
                        .build())
                .departure(buildDepartureDto(flightLog))
                .arrival(buildArrivalDto(flightLog))
                .aircraft(FlightLogResponseDto.AircraftDto.builder()
                        .aircraftNumber(flightLog.getAircraftNumber())
                        .aircraftType(flightLog.getAircraftType())
                        .build())
                .build());
    }

    @Transactional
    public ResponseEntity<FlightLogResponseDto.AggregateDto> updateFlightLog(Member member, UpdateFlightLogRequestDto updateFlightLogRequestDto) {
        Long flightLogId = updateFlightLogRequestDto.getFlightLogId();

        FlightLog flightLog = flightLogRepository.findById(flightLogId).orElseThrow(EntityNotFoundException::new);
        FlightLog updateFlightLog;

        String setDepAirportCode = updateFlightLogRequestDto.getDepAirportCode();
        String getDepAirportCode = flightLog.getDepAirportCode();

        String setArrivalAirportCode = updateFlightLogRequestDto.getArrivalAirportCode();
        String getArrivalAirportCode = flightLog.getArrivalAirportCode();

        LocalDate depDateUtc = updateFlightLogRequestDto.getDepDateUtc();
        LocalDate depDateLocal = updateFlightLogRequestDto.getDepDateLocal();
        LocalDate arrivalDateUtc = updateFlightLogRequestDto.getArrivalDateUtc();
        LocalDate arrivalDateLocal = updateFlightLogRequestDto.getArrivalDateLocal();
        String airline = updateFlightLogRequestDto.getAirline();
        String aircraftNumber = updateFlightLogRequestDto.getAircraftNumber();
        String aircraftType = updateFlightLogRequestDto.getAircraftType();
        String duty = updateFlightLogRequestDto.getDuty();

        LocalTime depScheduledTimeUtc = updateFlightLogRequestDto.getDepScheduledTimeUtc() == null ? flightLog.getDepScheduledTimeUtc() : updateFlightLogRequestDto.getDepScheduledTimeUtc();
        LocalTime depScheduledTimeLocal = updateFlightLogRequestDto.getDepScheduledTimeLocal() == null ? flightLog.getDepScheduledTimeLocal() : updateFlightLogRequestDto.getDepScheduledTimeLocal();
        LocalTime arrivalScheduledTimeUtc = updateFlightLogRequestDto.getArrivalScheduledTimeUtc() == null ? flightLog.getArrivalScheduledTimeUtc() : updateFlightLogRequestDto.getArrivalScheduledTimeUtc();
        LocalTime arrivalScheduledTimeLocal = updateFlightLogRequestDto.getArrivalScheduledTimeLocal() == null ? flightLog.getArrivalScheduledTimeLocal() : updateFlightLogRequestDto.getArrivalScheduledTimeLocal();

        LocalTime depActualTimeUtc = updateFlightLogRequestDto.getDepActualTimeUtc() == null ? depScheduledTimeUtc : updateFlightLogRequestDto.getDepActualTimeUtc();
        LocalTime depActualTimeLocal = updateFlightLogRequestDto.getDepActualTimeLocal() == null ? depScheduledTimeLocal : updateFlightLogRequestDto.getDepActualTimeLocal();
        LocalTime arrivalActualTimeUtc = updateFlightLogRequestDto.getDepActualTimeUtc() == null ? arrivalScheduledTimeUtc : updateFlightLogRequestDto.getDepActualTimeUtc();
        LocalTime arrivalActualTimeLocal = updateFlightLogRequestDto.getDepActualTimeLocal() == null ? arrivalScheduledTimeLocal : updateFlightLogRequestDto.getDepActualTimeLocal();


        updateFlightLog = flightLog.toBuilder()
                .flightId(flightLog.getFlightId())
                .flightDate(flightLog.getFlightDate())
                .depDateUtc(depDateUtc == null ? flightLog.getDepDateUtc() : depDateUtc)
                .depDateLocal(depDateLocal == null ? flightLog.getDepDateLocal() : depDateLocal)
                .arrivalDateUtc(arrivalDateUtc == null ? flightLog.getArrivalDateUtc() : arrivalDateUtc)
                .arrivalDateLocal(arrivalDateLocal == null ? flightLog.getArrivalDateLocal() : arrivalDateLocal)
                .airline(StringUtils.isEmptyOrWhitespace(airline) ? flightLog.getAirline() : airline)
                .aircraftNumber(StringUtils.isEmptyOrWhitespace(aircraftNumber) ? flightLog.getAircraftNumber() : aircraftNumber)
                .aircraftType(StringUtils.isEmptyOrWhitespace(aircraftType) ? flightLog.getAircraftType() : aircraftType)
                .duty(StringUtils.isEmptyOrWhitespace(duty) ? flightLog.getDuty() : duty)
                .depScheduledTimeLocal(depScheduledTimeLocal)
                .depScheduledTimeUtc(depScheduledTimeUtc)
                .arrivalScheduledTimeUtc(arrivalScheduledTimeUtc)
                .arrivalScheduledTimeLocal(arrivalScheduledTimeLocal)
                .depActualTimeUtc(depActualTimeUtc)
                .depActualTimeLocal(depActualTimeLocal)
                .arrivalActualTimeUtc(arrivalActualTimeUtc)
                .arrivalActualTimeLocal(arrivalActualTimeLocal)
                .flightTime(Duration.between(depActualTimeUtc, arrivalActualTimeUtc).toSeconds())
                .build();

        if (!StringUtils.isEmptyOrWhitespace(setDepAirportCode)) {
            if (setDepAirportCode.equals(setArrivalAirportCode))
                throw new IllegalArgumentException("DepAirportCode and ArrivalAirportCode cannot be the same");
            if (!setDepAirportCode.equals(getDepAirportCode)) {
                Airport airport = airportRepository.findById(setDepAirportCode).orElseThrow(
                        () -> new EntityNotFoundException("존재하지 않는 공항 코드 : " + setDepAirportCode));
                updateFlightLog = flightLog.toBuilder()
                        .depAirportCode(airport.getCode())
                        .depAirportName(airport.getName())
                        .depCountryCode(airport.getCountryCode())
                        .depCountryName(countryRepository.findById(airport.getCountryCode())
                                .orElseThrow(EntityNotFoundException::new)
                                .getCountryName())
                        .depAirportLocationLon(airport.getLon())
                        .depAirportLocationLat(airport.getLat())
                        .depAirportTimezone(airport.getTimeZone())
                        .build();
            }
        }
        if (!StringUtils.isEmptyOrWhitespace(setArrivalAirportCode) && !setArrivalAirportCode.equals(getArrivalAirportCode)) {
            Airport airport = airportRepository.findById(setArrivalAirportCode).orElseThrow(
                    () -> new EntityNotFoundException("존재하지 않는 공항 코드 : " + setArrivalAirportCode));
            updateFlightLog = flightLog.toBuilder()
                    .arrivalAirportCode(airport.getCode())
                    .arrivalAirportName(airport.getName())
                    .arrivalCountryCode(airport.getCountryCode())
                    .arrivalCountryName(countryRepository.findById(airport.getCountryCode())
                            .orElseThrow(EntityNotFoundException::new)
                            .getCountryName())
                    .arrivalAirportLocationLon(airport.getLon())
                    .arrivalAirportLocationLat(airport.getLat())
                    .arrivalAirportTimezone(airport.getTimeZone())
                    .build();
        }

        if (!StringUtils.isEmptyOrWhitespace(setDepAirportCode) && !setDepAirportCode.equals(getDepAirportCode) ||
                !StringUtils.isEmptyOrWhitespace(setArrivalAirportCode) && !setArrivalAirportCode.equals(getArrivalAirportCode)) {
            Double distanceKilometers = distanceInKilometerByHaversine(
                    flightLog.getDepAirportLocationLat(),
                    flightLog.getArrivalAirportLocationLat(),
                    flightLog.getDepAirportLocationLon(),
                    flightLog.getArrivalAirportLocationLon());
            flightLog.toBuilder()
                    .distanceKilometers(distanceKilometers)
                    .distanceMeters(distanceKilometers * 1000)
                    .distanceMiles(distanceKilometers * 0.621371)
                    .build();
        }

        flightLogRepository.save(updateFlightLog);

        return ResponseEntity.ok(FlightLogResponseDto.AggregateDto.builder()
                .flightLogId(updateFlightLog.getId())
                .memberId(updateFlightLog.getMemberId())
                .flightInfo(FlightLogResponseDto.FlightInfoDto.builder()
                        .flightId(updateFlightLog.getFlightId())
                        .airline(updateFlightLog.getAirline())
                        .build())
                .departure(buildDepartureDto(updateFlightLog))
                .arrival(buildArrivalDto(updateFlightLog))
                .aircraft(FlightLogResponseDto.AircraftDto.builder()
                        .aircraftNumber(updateFlightLog.getAircraftNumber())
                        .aircraftType(updateFlightLog.getAircraftType())
                        .build())
                .distance(FlightLogResponseDto.DistanceDto.builder()
                        .distanceKilometers(updateFlightLog.getDistanceKilometers())
                        .distanceMeters(updateFlightLog.getDistanceMeters())
                        .distanceMiles(updateFlightLog.getDistanceMiles())
                        .build())
                .flightTime(new FlightLogResponseDto.FlightTimeDto(updateFlightLog.getFlightTime()))
                .etcInfo(FlightLogResponseDto.EtcInfoDto.builder()
                        .duty(updateFlightLog.getDuty())
                        .crewMembers(updateFlightLogRequestDto.getCrewMembers() != null ? crewRepository.saveAll(updateFlightLogRequestDto.getCrewMembers()
                                        .stream().map(s -> Crew.builder()
                                                .name(s)
                                                .email(member.getEmail())
                                                .flightLogId(flightLogId)
                                                .build())
                                        .toList()).stream().map(c -> FlightLogResponseDto.CrewDto.builder()
                                        .crewName(c.getName())
                                        .build())
                                .toList() : List.of())
                        .build())
                .build());
    }

    private FlightLogResponseDto.DepartureDto buildDepartureDto(FlightLog flightLog) {
        return FlightLogResponseDto.DepartureDto.builder()
                .dateInfo(FlightLogResponseDto.DateInfoDto.builder()
                        .dateUtc(flightLog.getDepDateUtc())
                        .dateLocal(flightLog.getDepDateLocal())
                        .build())
                .airport(FlightLogResponseDto.AirportDto.builder()
                        .airportCode(flightLog.getDepAirportCode())
                        .airportName(flightLog.getDepAirportName())
                        .build())
                .country(FlightLogResponseDto.CountryDto.builder()
                        .countryCode(flightLog.getDepCountryCode())
                        .countryName(flightLog.getDepCountryName())
                        .build())
                .location(FlightLogResponseDto.LocationDto.builder()
                        .longitude(flightLog.getDepAirportLocationLon())
                        .latitude(flightLog.getDepAirportLocationLat())
                        .build())
                .timeZone(flightLog.getDepAirportTimezone())
                .scheduledTime(FlightLogResponseDto.ScheduledTimeDto.builder()
                        .scheduledTimeUtc(flightLog.getDepScheduledTimeUtc())
                        .scheduledTimeLocal(flightLog.getDepScheduledTimeLocal())
                        .build())
                .build();
    }

    private FlightLogResponseDto.ArrivalDto buildArrivalDto(FlightLog flightLog) {
        return FlightLogResponseDto.ArrivalDto.builder()
                .dateInfo(FlightLogResponseDto.DateInfoDto.builder()
                        .dateUtc(flightLog.getArrivalDateUtc())
                        .dateLocal(flightLog.getArrivalDateLocal())
                        .build())
                .airport(FlightLogResponseDto.AirportDto.builder()
                        .airportCode(flightLog.getArrivalAirportCode())
                        .airportName(flightLog.getArrivalAirportName())
                        .build())
                .country(FlightLogResponseDto.CountryDto.builder()
                        .countryCode(flightLog.getArrivalCountryCode())
                        .countryName(flightLog.getArrivalCountryName())
                        .build())
                .location(FlightLogResponseDto.LocationDto.builder()
                        .longitude(flightLog.getArrivalAirportLocationLon())
                        .latitude(flightLog.getArrivalAirportLocationLat())
                        .build())
                .timeZone(flightLog.getArrivalAirportTimezone())
                .scheduledTime(FlightLogResponseDto.ScheduledTimeDto.builder()
                        .scheduledTimeUtc(flightLog.getArrivalScheduledTimeUtc())
                        .scheduledTimeLocal(flightLog.getArrivalScheduledTimeLocal())
                        .build())
                .build();
    }

    public static Double distanceInKilometerByHaversine(double lat1, double lat2, double lon1, double lon2) {
        final double RADIUS = 6371;
        double dLat = Math.toRadians(lat1 - lat2);
        double dLon = Math.toRadians(lon1 - lon2);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return RADIUS * 2 * Math.asin(Math.sqrt(a));
    }
}
