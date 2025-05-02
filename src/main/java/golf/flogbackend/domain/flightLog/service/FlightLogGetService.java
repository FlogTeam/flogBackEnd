package golf.flogbackend.domain.flightLog.service;

import golf.flogbackend.domain.airport.entity.Airport;
import golf.flogbackend.domain.country.entity.Country;
import golf.flogbackend.domain.crew.entity.Crew;
import golf.flogbackend.domain.crew.repository.CrewRepository;
import golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto;
import golf.flogbackend.domain.flightLog.dto.FlightLogSummaryResponseDto;
import golf.flogbackend.domain.flightLog.entity.*;
import golf.flogbackend.domain.flightLog.repository.FlightLogRepository;
import golf.flogbackend.domain.flightLog.support.DutyCountByAircraftType;
import golf.flogbackend.domain.flightLog.support.FlightLogResponseDtoMapper;
import golf.flogbackend.domain.flightLog.support.FlightLogUtil;
import golf.flogbackend.domain.member.entity.Member;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static golf.flogbackend.domain.flightLog.support.FlightLogResponseDtoMapper.*;
import static golf.flogbackend.domain.flightLog.support.FlightLogUtil.parseDateOrDefault;
import static golf.flogbackend.exception.ErrorCode.INVALID_DATE_ORDER;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightLogGetService {
    @Value("${api.key}")
    private String apiKey;
    private final FlightLogRepository flightLogRepository;
    private final CrewRepository crewRepository;
    private final FlightLogUtil flightLogUtil;

    public ResponseEntity<List<FlightLogResponseDto.FlightResponseDto>> getFlightList(String flightId, String searchFlightDate) throws ParseException {
        LocalDate flightDate = parseDateOrDefault(searchFlightDate, null);

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
        try {
            flightData = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        } catch (HttpClientErrorException e) {
            throw new EntityNotFoundException("검색 실패 : " + flightId + " / " + flightDate);
        }

        String body = flightData.getBody();
        if (body == null || body.isEmpty()) throw new EntityNotFoundException("응답 데이터 없음");

        JSONParser jsonParser = new JSONParser();
        JSONArray flightArray = (JSONArray) jsonParser.parse(body);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mmXXX");
        List<FlightLogResponseDto.FlightResponseDto> responseList = new ArrayList<>();


        for (Object obj : flightArray) {
            JSONObject jsonObj = (JSONObject) obj;

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

            FlightLog flightLog = FlightLog.builder()
                    .flightId(flightId)
                    .flightDate(flightDate)
                    .airline((String) ((JSONObject) jsonObj.get("airline")).get("name"))
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
                            .cityNameKorean(depAirport.getCityNameKorean())
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
                            .cityNameKorean(arriAirport.getCityNameKorean())
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
                    .build();

            responseList.add(FlightLogResponseDto.FlightResponseDto.builder()
                    .flightInfo(buildFlightInfoDto(flightLog))
                    .departure(buildDepartureDto(flightLog))
                    .arrival(buildArrivalDto(flightLog))
                    .aircraft(buildAircraftDto(flightLog.getAircraft()))
                    .distance(buildDistanceDto(flightLog.getDistance()))
                    .build());
        }

        return ResponseEntity.ok(responseList);
    }

    public ResponseEntity<List<FlightLogSummaryResponseDto>> getFlightLogSummaryList(Member member) {
        return ResponseEntity.ok(flightLogRepository.findByMemberIdOrderByCreateAtDesc(member.getEmail())
                .stream().map(FlightLogResponseDtoMapper::buildFlightLogSummaryResponseDto)
                .toList());
    }

    public ResponseEntity<FlightLogResponseDto.FlightLogAllInfoDto> getFlightLog(Long flightLogId) {
        return ResponseEntity.ok(buildFlightLogAllInfoDto(
                flightLogUtil.findFlightLogById(flightLogId),
                crewRepository.findByFlightLogId(flightLogId)));
    }

    public ResponseEntity<FlightLogResponseDto.FlightLogDataDto> getFlightLogData(Member member, String startDate, String endDate) {
        LocalDate parsedStartDate = parseDateOrDefault(startDate, LocalDate.EPOCH);
        LocalDate parsedEndDate = parseDateOrDefault(endDate, LocalDate.now());
        if (!parsedStartDate.isBefore(parsedEndDate))
            throw new IllegalArgumentException(INVALID_DATE_ORDER.code() + "startDate must be before endDate");

        List<FlightLog> flightLogList = flightLogRepository.findByMemberIdAndFlightDateBetween(member.getEmail(), parsedStartDate, parsedEndDate);

        return ResponseEntity.ok(FlightLogResponseDtoMapper.buildFlightLogDataDto(
                flightLogList,
                flightLogRepository.findDutyStatsGroupedByAircraftType(member.getEmail(), parsedStartDate, parsedEndDate).stream()
                        .collect(Collectors.groupingBy(DutyCountByAircraftType::getAircraftType))
                        .entrySet()
                        .stream()
                        .sorted(Comparator.comparingLong(
                                (Map.Entry<String, List<DutyCountByAircraftType>> entry) ->
                                        entry.getValue().stream()
                                                .mapToLong(DutyCountByAircraftType::getCount)
                                                .sum()
                        ).reversed())
                        .map(entry -> FlightLogResponseDto.DutyByAircraftTypeDto.builder()
                                .aircraftType(entry.getKey())
                                .dutyTotalCount(entry.getValue()
                                        .stream()
                                        .mapToLong(DutyCountByAircraftType::getCount).sum())
                                .dutyByAircraftType(
                                        entry.getValue().stream()
                                                .sorted(Comparator.comparingLong(DutyCountByAircraftType::getCount).reversed())
                                                .map(d -> new FlightLogResponseDto.DutyDto(
                                                        d.getDuty() != null ? d.getDuty() : "UNKNOWN", d.getCount()))
                                                .toList()
                                )
                                .build()
                        )
                        .toList(),
                crewRepository.findByEmail(member.getEmail()).stream()
                        .collect(Collectors.groupingBy(Crew::getName, Collectors.counting()))
                        .entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .map(e -> FlightLogResponseDto.CrewMateDto.builder()
                                .name(e.getKey())
                                .count(e.getValue())
                                .build())
                        .toList()));
    }
}
