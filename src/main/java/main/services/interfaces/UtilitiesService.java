package main.services.interfaces;
import main.model.enums.ModerationStatus;

import java.time.LocalDateTime;

public interface UtilitiesService {

    String getRandomHash(int n);
    long getTimestampFromLocalDateTime(LocalDateTime localDateTime);
    LocalDateTime getLocalDateTimeFromTimestamp(long timestamp);
    LocalDateTime setRightTime(LocalDateTime localDateTime);
    LocalDateTime convertLocalTimeToUtc(LocalDateTime localDateTime);

    byte getIsActive();
    LocalDateTime getTime();
    ModerationStatus getModerationStatus();
}
