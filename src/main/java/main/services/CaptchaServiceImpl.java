package main.services;

import com.github.cage.YCage;
import main.api.responses.AuthResponseBody;
import main.model.CaptchaCode;
import main.repositories.CaptchaCodeRepository;
import main.services.interfaces.CaptchaService;
import main.services.interfaces.UtilitiesService;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class CaptchaServiceImpl implements CaptchaService {

   // @Value("${captcha.hour:2}")
    private LocalDateTime captchaLifetime;

    @Value("${captcha.width}")
    int width;

    @Value("${captcha.height}")
    int height;

    @Value("${captcha.prefix}")
    String captchaPrefix;

    @Value("${captcha.format}")
    String captchaFormat;

    private final CaptchaCodeRepository captchaCodeRepository;
    private final UtilitiesService utilitiesService;

    @Autowired
    public CaptchaServiceImpl(CaptchaCodeRepository captchaCodeRepository,
                              UtilitiesService utilitiesService) {
        this.captchaCodeRepository = captchaCodeRepository;
        this.utilitiesService = utilitiesService;
    }

    @Override
    public ResponseEntity<AuthResponseBody> getCaptcha() throws IOException {

        String secretCode = utilitiesService.getRandomHash(22);
        String captchaCode = utilitiesService.getRandomHash(4);
        String code = Base64.getEncoder().encodeToString(captchaCode.getBytes());
        captchaCodeRepository.save(CaptchaCode.builder()
                .time(LocalDateTime.now())
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
}
