package main.services.interfaces;
import main.api.responses.ApiResponseBody;
import main.api.responses.bodies.ErrorsBody;
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
    boolean isNameIncorrect(String name);
    boolean isPasswordShort(String password);
    String encodePassword(String password);
    boolean isUserTypeCorrectPassword(String typedPassword, String passwordInDatabase);
    ApiResponseBody getErrorResponse(ErrorsBody errors);
    ApiResponseBody getShortPasswordErrorResponse();
    ApiResponseBody getIncorrectNameErrorResponse();
    ApiResponseBody getIncorrectEmailErrorResponse();

    byte getIsActive();
    LocalDateTime getTime();
    String getModerationStatus();
}
