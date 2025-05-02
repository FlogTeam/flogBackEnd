package golf.flogbackend.domain.flightLog.enums;

import golf.flogbackend.domain.flightLog.entity.FlightLog;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum LocationType {
    AIRPORT(flightLog -> Pair.of(
            Triple.of(flightLog.getDeparture().getAirportName(), flightLog.getDeparture().getAirportCode(), flightLog.getDeparture().getAirportNameKorean()),
            Triple.of(flightLog.getArrival().getAirportName(), flightLog.getArrival().getAirportCode(), flightLog.getArrival().getAirportNameKorean())
    )),
    CITY(flightLog -> Pair.of(
            Triple.of(flightLog.getDeparture().getCityName(), flightLog.getDeparture().getCityCode(), flightLog.getDeparture().getCityNameKorean()),
            Triple.of(flightLog.getArrival().getCityName(), flightLog.getArrival().getCityCode(), flightLog.getArrival().getCityNameKorean())
    )),
    COUNTRY(flightLog -> Pair.of(
            Triple.of(flightLog.getDeparture().getCountryName(), flightLog.getDeparture().getCountryCode(), flightLog.getDeparture().getCountryNameKorean()),
            Triple.of(flightLog.getArrival().getCountryName(), flightLog.getArrival().getCountryCode(), flightLog.getArrival().getCountryNameKorean())
    ));

    private final Function<FlightLog, Pair<Triple<String, String, String>, Triple<String, String, String>>> classifier;

    public Pair<Triple<String, String, String>, Triple<String, String, String>> classify(FlightLog flightLog) {
        return classifier.apply(flightLog);
    }
}
