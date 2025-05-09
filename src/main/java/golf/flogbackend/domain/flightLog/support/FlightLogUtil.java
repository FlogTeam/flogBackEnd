package golf.flogbackend.domain.flightLog.support;

import golf.flogbackend.domain.airport.entity.Airport;
import golf.flogbackend.domain.airport.repository.AirportRepository;
import golf.flogbackend.domain.country.entity.Country;
import golf.flogbackend.domain.country.repository.CountryRepository;
import golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto;
import golf.flogbackend.domain.flightLog.entity.FlightLog;
import golf.flogbackend.domain.flightLog.enums.Endpoint;
import golf.flogbackend.domain.flightLog.repository.FlightLogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static golf.flogbackend.exception.ErrorCode.*;

@Component
@RequiredArgsConstructor
public class FlightLogUtil {

    private final AirportRepository airportRepository;
    private final CountryRepository countryRepository;
    private final FlightLogRepository flightLogRepository;

    public FlightLog findFlightLogById(Long flightLogId) {
        return flightLogRepository.findById(flightLogId).orElseThrow(
                () -> new EntityNotFoundException(NOT_FOUND_FLIGHT_LOG.code() + "존재하지 않는 flight log id : " + flightLogId));
    }

    public Airport findAirportByCode(String code) {
        return airportRepository.findById(code).orElseThrow(
                () -> new EntityNotFoundException(NOT_FOUND_AIRPORT.getCode() + "존재하지 않는 공항 코드 : " + code));
    }

    public Country findCountryByCode(String code) {
        return countryRepository.findById(code).orElseThrow(
                () -> new EntityNotFoundException(NOT_FOUND_COUNTRY.getCode() + "존재하지 않는 나라 코드 : " + code));
    }

    public static FlightEndpoint getEndpoint(FlightLog flightLog, Endpoint endpoint) {
        return switch (endpoint) {
            case DEPARTURE -> flightLog.getDeparture();
            case ARRIVAL -> flightLog.getArrival();
        };
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

    public static List<FlightLogResponseDto.VisitedDataDto> createVisitedDataList(List<FlightLog> flightLogList,
                                                                                  Function<FlightLog, Triple<String, String, String>> classifier) {
        return flightLogList.stream()
                .map(classifier)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<Triple<String, String, String>, Long>comparingByValue().reversed())
                .map(entry -> FlightLogResponseDto.VisitedDataDto.builder()
                        .name(entry.getKey().getLeft())
                        .code(entry.getKey().getMiddle())
                        .nameKorean(entry.getKey().getRight())
                        .count(entry.getValue())
                        .percentage(round((entry.getValue() * 100.0) / flightLogList.size()))
                        .build())
                .toList();
    }

    public static List<FlightLogResponseDto.VisitedDataDto> concatVisitedDataList(Stream<FlightLogResponseDto.VisitedDataDto> streamOne,
                                                                                  Stream<FlightLogResponseDto.VisitedDataDto> streamTwo,
                                                                                  long size) {
        return Stream.concat(streamOne, streamTwo)
                .collect(Collectors.groupingBy(
                        FlightLogResponseDto.VisitedDataDto::getName,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    long count = list.stream().mapToLong(FlightLogResponseDto.VisitedDataDto::getCount).sum();
                                    return FlightLogResponseDto.VisitedDataDto.builder()
                                            .name(!list.isEmpty() ? list.getFirst().getName() : "")
                                            .code(!list.isEmpty() ? list.getFirst().getCode() : "")
                                            .nameKorean(!list.isEmpty() ? list.getFirst().getNameKorean() : "")
                                            .count(count)
                                            .percentage(round((count * 100.0) / (size * 2)))
                                            .build();
                                })))
                .values().stream()
                .sorted(Comparator.comparingLong(FlightLogResponseDto.VisitedDataDto::getCount).reversed())
                .toList();
    }

    public static LocalDate parseDateOrDefault(String dateStr, LocalDate defaultDate) {
        if (StringUtils.isEmptyOrWhitespace(dateStr)) return defaultDate;
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(INVALID_DATE_FORMAT.code() + "날짜 포맷이 올바르지 않습니다. yyyy-MM-dd 형식이어야 합니다. 입력값: " + dateStr);
        }
    }

    public static LocalTime parseTimeOrDefault(String timeStr, LocalTime defaultTime) {
        if (StringUtils.isEmptyOrWhitespace(timeStr)) return defaultTime;
        try {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(INVALID_TIME_FORMAT.code() + "시간 포맷이 올바르지 않습니다. HH:mm 형식이어야 합니다. 입력값: " + timeStr);
        }
    }

    public static Double round(Double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
