package main.services;

import com.github.cage.YCage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.api.responses.AuthResponseBody;
import main.model.CaptchaCode;
import main.repositories.CaptchaCodeRepository;
import main.services.interfaces.CaptchaService;
import main.services.interfaces.UtilitiesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {

    @Value("${captcha.width}")
    private int width;

    @Value("${captcha.height}")
    private int height;

    @Value("${captcha.prefix}")
    private String captchaPrefix;

    @Value("${captcha.format}")
    private String captchaFormat;

    @Value("${captcha.lifetime: 1}")
    private long captchaLifetime;

    @Value("${captcha.code.length}")
    private int captchaCodeLength;

    @Value("${captcha.secret.code.length}")
    private int captchaSecretCodeLength;

    private final CaptchaCodeRepository captchaCodeRepository;
    private final UtilitiesService utilitiesService;

    @Override
    public ResponseEntity<AuthResponseBody> getCaptcha()
    {
        deleteOldCaptchas();
        String secretCode = utilitiesService.getRandomHash(captchaSecretCodeLength);
        String captchaCode = utilitiesService.getRandomHash(captchaCodeLength);
        LocalDateTime time = utilitiesService.getTime();
        String code = Base64.getEncoder().encodeToString(captchaCode.getBytes());
        captchaCodeRepository.save(CaptchaCode.builder()
                .time(time)
                .code(code)
                .secretCode(secretCode)
                .build());

        BufferedImage image = new YCage().drawImage(captchaCode);
        if (image.getWidth() > width && image.getHeight() > height) {
            int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();
            BufferedImage resizeImage = new BufferedImage(width, height, type);
            Graphics2D g = resizeImage.createGraphics();
            g.drawImage(image, 0, 0, width, height, null);
            g.dispose();
            image = resizeImage;
        }
        byte[] imageBytes = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, captchaFormat, baos);
            imageBytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String imageCode = Base64.getEncoder().encodeToString(imageBytes);
        String result = captchaPrefix + imageCode;

        return ResponseEntity.ok(AuthResponseBody.builder().secret(secretCode).image(result).build());
    }

    @Override
    public boolean checkCaptchaLifetime(String code) {
        CaptchaCode captchaCode = captchaCodeRepository.findByCode(code);
        LocalDateTime captchaCreateTime = captchaCode.getTime();
        LocalDateTime currentTime = utilitiesService.getTime();

        return captchaCreateTime.plusHours(captchaLifetime).isBefore(currentTime);
    }

    private void deleteOldCaptchas() {
        LocalDateTime currentTime = utilitiesService.getTime();
        List<CaptchaCode> oldCaptchas = captchaCodeRepository.findByTimeBefore(currentTime.minusHours(captchaLifetime));
        log.info("Old captchas find and delete: {}", oldCaptchas.size());

        oldCaptchas.forEach(captchaCode -> captchaCodeRepository.deleteById(captchaCode.getId()));
    }
}
