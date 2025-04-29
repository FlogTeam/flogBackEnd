package golf.flogbackend.domain.flightLog.support;

import golf.flogbackend.domain.flightLog.entity.FlightLog;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum LocationType {
    AIRPORT(flightLog -> Triple.of(flightLog.getDeparture().getAirportName(), flightLog.getDeparture().getAirportCode(), flightLog.getDeparture().getAirportNameKorean())),
    CITY(flightLog -> Triple.of(flightLog.getDeparture().getCityName(), flightLog.getDeparture().getCityCode(), flightLog.getDeparture().getCityNameKorean())),
    COUNTRY(flightLog -> Triple.of(flightLog.getDeparture().getCountryName(), flightLog.getDeparture().getCountryCode(), flightLog.getDeparture().getCountryNameKorean()));

    private final Function<FlightLog, Triple<String, String, String>> classifier;

}
