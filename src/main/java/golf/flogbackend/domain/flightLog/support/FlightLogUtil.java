package golf.flogbackend.domain.flightLog.support;

import golf.flogbackend.domain.airport.entity.Airport;
import golf.flogbackend.domain.airport.repository.AirportRepository;
import golf.flogbackend.domain.country.entity.Country;
import golf.flogbackend.domain.country.repository.CountryRepository;
import golf.flogbackend.domain.flightLog.dto.FlightLogResponseDto;
import golf.flogbackend.domain.flightLog.entity.FlightLog;
import golf.flogbackend.domain.flightLog.repository.FlightLogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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

    public static FlightEndpoint getEndpoint(FlightLog flightLog, EndpointEnum endpoint) {
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
                                                                                  Function<FlightLog, String> classifier) {
        return flightLogList.stream()
                .map(classifier)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> FlightLogResponseDto.VisitedDataDto.builder()
                        .name(entry.getKey())
                        .count(entry.getValue())
                        .percentage((entry.getValue() * 100.0) / flightLogList.size())
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
                                            .count(count)
                                            .percentage((count * 100.0) / (size * 2))
                                            .build();
                                })))
                .values().stream()
                .sorted(Comparator.comparingLong(FlightLogResponseDto.VisitedDataDto::getCount).reversed())
                .toList();
    }

}
