package golf.flogbackend.domain.flightLog.support;

public interface DutyCountByAircraftType {
    String getAircraftType();
    String getDuty();
    Long getCount();
}