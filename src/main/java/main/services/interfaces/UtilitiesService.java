package main.services.interfaces;
import main.model.enums.ModerationStatus;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;

public interface UtilitiesService {

    String getRandomHash(int n);
    long getTimestampFromLocalDateTime(LocalDateTime localDateTime);
    LocalDateTime getLocalDateTimeFromTimestamp(long timestamp);
    LocalDateTime setRightTime(LocalDateTime localDateTime);
    LocalDateTime convertLocalTimeToUtc(LocalDateTime localDateTime);
    BufferedImage imageResizer(BufferedImage image);
    boolean isEmailCorrect(String email);
    boolean isNameCorrect(String name);
    boolean isPasswordNotShort(String password);
    String encodePassword(String password);
    boolean isUserTypeCorrectPassword(String typedPassword, String passwordInDatabase);

    byte getIsActive();
    LocalDateTime getTime();
    ModerationStatus getModerationStatus();
}
