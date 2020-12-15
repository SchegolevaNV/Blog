package main.services;

import lombok.Data;
import main.api.responses.ApiResponseBody;
import main.api.responses.bodies.ErrorsBody;
import main.model.enums.Errors;
import main.services.interfaces.UtilitiesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Random;

@Service
@Data
public class UtilitiesServiceImpl implements UtilitiesService {

    private final BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();

    @Value("${is.active}")
    private byte isActive;

    @Value("${moderation.status}")
    private String moderationStatus;

    @Value("${avatar.width}")
    private int avatarWidth;

    @Value("${avatar.height}")
    private int avatarHeight;

    @Value("${name.max.length}")
    private int nameMaxLength;

    @Value("${min.password.length}")
    private int minPasswordLength;

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

    public BufferedImage imageResizer(BufferedImage image)
    {
        BufferedImage newImage =
                new BufferedImage(avatarWidth, avatarHeight, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < avatarWidth; x++) {
            for (int y = 0; y < avatarHeight; y++) {
                int rgb = image.getRGB(x * image.getWidth() / avatarWidth, y *  image.getHeight() / avatarHeight);
                newImage.setRGB(x, y, rgb);
            }
        }
        return newImage;
    }

    public boolean isEmailCorrect(String email) {
        return email.matches("[aA-zZ0-9_\\-.]+@[a-z0-9]+\\.[a-z]+");
    }

    public boolean isNameCorrect(String name) {
        return name.length() < nameMaxLength || name.matches("[aA-zZаА-яЯ0-9_\\- ]+");
    }

    public boolean isPasswordNotShort(String password) {
        return password.length() >= minPasswordLength;
    }

    public String encodePassword(String password)
    {
        return bcryptEncoder.encode(password);
    }

    public boolean isUserTypeCorrectPassword(String typedPassword, String passwordInDatabase) {
        return bcryptEncoder.matches(typedPassword, passwordInDatabase);
    }

    public byte getIsActive() {
        return isActive;
    }

    public LocalDateTime getTime() {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }

    public String getModerationStatus() {
        return moderationStatus;
    }

    public ApiResponseBody getErrorResponse(ErrorsBody errors) {
        return ApiResponseBody.builder()
                .result(false)
                .errors(errors)
                .build();
    }
    public ApiResponseBody getShortPasswordErrorResponse() {
        return ApiResponseBody.builder()
                .result(false)
                .errors(ErrorsBody.builder()
                        .password(Errors.PASSWORD_IS_SHORT.getTitle())
                        .build())
                .build();
    }
    public ApiResponseBody getIncorrectNameErrorResponse() {
        return ApiResponseBody.builder()
                .result(false)
                .errors(ErrorsBody.builder()
                        .name(Errors.NAME_IS_INCORRECT.getTitle())
                        .build())
                .build();
    }

    public ApiResponseBody getIncorrectEmailErrorResponse() {
        return ApiResponseBody.builder()
                .result(false)
                .errors(ErrorsBody.builder()
                        .email(Errors.EMAIL_IS_INCORRECT.getTitle())
                        .build())
                .build();
    }
}
