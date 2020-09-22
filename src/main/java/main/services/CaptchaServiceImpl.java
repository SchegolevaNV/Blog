package main.services;

import com.github.cage.YCage;
import main.api.responses.AuthResponseBody;
import main.model.CaptchaCode;
import main.repositories.CaptchaCodeRepository;
import main.services.interfaces.CaptchaService;
import main.services.interfaces.UtilitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private final CaptchaCodeRepository captchaCodeRepository;
    private final UtilitiesService utilitiesService;

    @Autowired
    public CaptchaServiceImpl(CaptchaCodeRepository captchaCodeRepository,
                              UtilitiesService utilitiesService) {
        this.captchaCodeRepository = captchaCodeRepository;
        this.utilitiesService = utilitiesService;
    }

    @Override
    public AuthResponseBody getCaptcha() throws IOException {

        String secretCode = utilitiesService.getRandomHash(22);
        String captchaCode = utilitiesService.getRandomHash(4);
        String code = Base64.getEncoder().encodeToString(captchaCode.getBytes());
        captchaCodeRepository.save(CaptchaCode.builder()
                .time(LocalDateTime.now())
                .code(code)
                .secretCode(secretCode)
                .build());

        int WIDTH = 100;
        int HEIGHT = 35;

        BufferedImage image = new YCage().drawImage(captchaCode);
        if (image.getWidth() > WIDTH && image.getHeight() > HEIGHT) {
            int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();
            BufferedImage resizeImage = new BufferedImage(WIDTH, HEIGHT, type);
            Graphics2D g = resizeImage.createGraphics();
            g.drawImage(image, 0, 0, WIDTH, HEIGHT, null);
            g.dispose();
            image = resizeImage;
        }
        byte[] imageBytes = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            imageBytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String imageCode = Base64.getEncoder().encodeToString(imageBytes);
        String result = "data:image/png;base64, " + imageCode;

        return AuthResponseBody.builder().secret(secretCode).image(result).build();
    }
}
