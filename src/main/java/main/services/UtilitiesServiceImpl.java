package main.services;

import lombok.Data;
import main.services.interfaces.UtilitiesService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Random;

@Service
@Data
public class UtilitiesServiceImpl implements UtilitiesService {

    public final ZoneId TIME_ZONE = ZoneId.of("UTC");
    public final ZoneOffset ZONE_OFFSET = ZoneOffset.UTC;

    public String getRandomHash(int n) {
        byte[] array = new byte[512];
        new Random().nextBytes(array);

        String hash = new String(array, StandardCharsets.UTF_8);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < hash.length(); i++) {
            char ch = hash.charAt(i);
            if (((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')) && (n > 0)) {
                result.append(ch);
                n--;
            }
        }
        return result.toString();
    }

    public long getTimestampFromLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime.toEpochSecond(ZONE_OFFSET);
    }

    public LocalDateTime getLocalDateTimeFromTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), TIME_ZONE);
    }

    public LocalDateTime convertLocalTimeToUtc(LocalDateTime localDateTime) {
        ZonedDateTime localZone = localDateTime.atZone(ZoneId.systemDefault());
        ZonedDateTime utcZone = localZone.withZoneSameInstant(TIME_ZONE);
        return utcZone.toLocalDateTime();
    }

    public LocalDateTime setRightTime(LocalDateTime localDateTime) {
        return (localDateTime.isBefore(LocalDateTime.now(TIME_ZONE)))
                ? LocalDateTime.now(TIME_ZONE)
                : localDateTime;
        }
}
