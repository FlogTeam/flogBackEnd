package golf.flogbackend.domain.flightLog.service;

import golf.flogbackend.domain.country.repository.CountryRepository;
import golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto;
import golf.flogbackend.domain.flightLog.dto.SaveFlightLogStepOneRequestDto;
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

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.TimeZone;

@Service
@RequiredArgsConstructor
public class FlightLogService {
    @Value("${api.key}")
    private String apiKey;
    private final FlightLogRepository flightLogRepository;
    private final CountryRepository countryRepository;

    @Transactional
    public ResponseEntity<FlightLogResponseDto.AggregateDto> saveFlightLogStepOne(Member member, SaveFlightLogStepOneRequestDto saveFlightLogStepOneRequestDto) throws ParseException {
        String flightId = saveFlightLogStepOneRequestDto.getFlightId();
        LocalDate flightDate = saveFlightLogStepOneRequestDto.getFlightDate();

        Optional<FlightLog> flightLogCheck = flightLogRepository.findByMemberIdAndFlightIdAndFlightDate(member.getEmail(), flightId, flightDate);
        if (flightLogCheck.isPresent()) throw new EntityExistsException("flightLogId : " + flightLogCheck.get().getId());

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

        ZonedDateTime depRunwayTimeUtc = ZonedDateTime.parse((String) ((JSONObject) departureData.get("runwayTime")).get("utc"), formatter);
        ZonedDateTime arrivalPredictedTimeUtc = ZonedDateTime.parse((String) ((JSONObject) arrivalData.get("predictedTime")).get("utc"), formatter);

        FlightLog flightLog = flightLogRepository.save(FlightLog.builder()
                .flightId(saveFlightLogStepOneRequestDto.getFlightId())
                .flightDate(saveFlightLogStepOneRequestDto.getFlightDate())
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
                .depScheduledTimeLocal(ZonedDateTime.parse((String) ((JSONObject) departureData.get("scheduledTime")).get("local"), formatter))
                .depScheduledTimeUtc(ZonedDateTime.parse((String) ((JSONObject) departureData.get("scheduledTime")).get("utc"), formatter))
                .depRevisedTimeUtc(ZonedDateTime.parse((String) ((JSONObject) departureData.get("revisedTime")).get("utc"), formatter))
                .depRevisedTimeLocal(ZonedDateTime.parse((String) ((JSONObject) departureData.get("revisedTime")).get("local"), formatter))
                .depRunwayTimeUtc(depRunwayTimeUtc)
                .depRunwayTimeLocal(ZonedDateTime.parse((String) ((JSONObject) departureData.get("runwayTime")).get("local"), formatter))
                .arrivalAirportCode((String) arrivalAirportData.get("iata"))
                .arrivalAirportName((String) arrivalAirportData.get("name"))
                .arrivalCountryCode(arrivalCountryCode)
                .arrivalCountryName(countryRepository.findById(arrivalCountryCode ).orElseThrow(
                                EntityNotFoundException::new)
                        .getCountryName())
                .arrivalAirportLocationLon((Double) ((JSONObject) arrivalAirportData.get("location")).get("lon"))
                .arrivalAirportLocationLat((Double) ((JSONObject) arrivalAirportData.get("location")).get("lat"))
                .arrivalAirportTimezone(TimeZone.getTimeZone((String) arrivalAirportData.get("timeZone")))
                .arrivalScheduledTimeUtc(ZonedDateTime.parse((String) ((JSONObject) arrivalData.get("scheduledTime")).get("utc"), formatter))
                .arrivalScheduledTimeLocal(ZonedDateTime.parse((String) ((JSONObject) arrivalData.get("scheduledTime")).get("local"), formatter))
                .arrivalPredictedTimeUtc(arrivalPredictedTimeUtc)
                .arrivalPredictedTimeLocal(ZonedDateTime.parse((String) ((JSONObject) arrivalData.get("predictedTime")).get("local"), formatter))
                .distanceKilometers((Double) greatCircleDistanceData.get("km"))
                .distanceMeters((Double) greatCircleDistanceData.get("meter"))
                .distanceMiles((Double) greatCircleDistanceData.get("mile"))
                .flightTime(Duration.between(depRunwayTimeUtc, arrivalPredictedTimeUtc).toSeconds())
                .build());

        return ResponseEntity.ok(FlightLogResponseDto.AggregateDto.builder()
                .flightLogId(flightLog.getId())
                .memberId(flightLog.getMemberId())
                .flightInfo(FlightLogResponseDto.FlightInfoDto.builder()
                        .flightId(flightLog.getFlightId())
                        .flightDate(flightLog.getFlightDate())
                        .airline(flightLog.getAirline())
                        .duty(flightLog.getDuty())
                        .build())
                .departure(FlightLogResponseDto.DepartureDto.builder()
                        .airportDto(FlightLogResponseDto.AirportDto.builder()
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
                        .revisedTime(FlightLogResponseDto.RevisedTimeDto.builder()
                                .RevisedTimeUtc(flightLog.getDepRevisedTimeUtc())
                                .RevisedTimeLocal(flightLog.getDepRevisedTimeLocal())
                                .build())
                        .runawayTime(FlightLogResponseDto.RunawayTimeDto.builder()
                                .runawayTimeUtc(flightLog.getDepRunwayTimeUtc())
                                .runawayTimeLocal(flightLog.getDepRunwayTimeLocal())
                                .build())
                        .build())
                .arrival(FlightLogResponseDto.ArrivalDto.builder()
                        .airportDto(FlightLogResponseDto.AirportDto.builder()
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
                        .predictedTime(FlightLogResponseDto.PredictedTimeDto.builder()
                                .predictedTimeUtc(flightLog.getArrivalPredictedTimeUtc())
                                .predictedTimeLocal(flightLog.getArrivalPredictedTimeLocal())
                                .build())
                        .build())
                .aircraft(FlightLogResponseDto.AircraftDto.builder()
                        .aircraftNumber(flightLog.getAircraftNumber())
                        .aircraftType(flightLog.getAircraftType())
                        .build())
                .distance(FlightLogResponseDto.DistanceDto.builder()
                        .distanceKilometers(flightLog.getDistanceKilometers())
                        .distanceMeters(flightLog.getDistanceMeters())
                        .distanceMiles(flightLog.getDistanceMiles())
                        .build())
                .flightTime(new FlightLogResponseDto.FlightTimeDto(flightLog.getFlightTime()))
                .build());
    }
}
