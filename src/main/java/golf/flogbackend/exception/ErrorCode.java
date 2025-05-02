package golf.flogbackend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    NOT_FOUND_FLIGHT_LOG("code : 1"),
    NOT_FOUND_AIRPORT("code : 2"),
    NOT_FOUND_COUNTRY("code : 3"),

    SAME_DEPARTURE_ARRIVAL_AIRPORT("code : 4"),

    ARRIVAL_BEFORE_DEPARTURE_ACTUAL_UTC("code : 5"),
    ARRIVAL_BEFORE_DEPARTURE_ACTUAL_LOCAL("code : 6"),
    ARRIVAL_BEFORE_DEPARTURE_SCHEDULED_UTC("code : 7"),
    ARRIVAL_BEFORE_DEPARTURE_SCHEDULED_LOCAL("code : 8"),

    INVALID_DATE_FORMAT("code : 9"),
    INVALID_TIME_FORMAT("code : 10"),

    INVALID_DATE_ORDER("code : 11");

    private final String code;

    public String code() {
        return code;
    }
}
