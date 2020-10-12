package main.services.interfaces;
import java.time.LocalDateTime;

public interface UtilitiesService {

    String getRandomHash(int n);
    long getTimestampFromLocalDateTime(LocalDateTime localDateTime);
    LocalDateTime getLocalDateTimeFromTimestamp(long timestamp);
    LocalDateTime setRightTime(LocalDateTime localDateTime);
    LocalDateTime convertLocalTimeToUtc(LocalDateTime localDateTime);
}
