package main.services;

import com.github.cage.YCage;
import main.model.CaptchaCode;
import main.repositories.CaptchaCodeRepository;
import main.api.requests.AuthRequestBody;
import main.api.responses.AuthResponseBody;
import main.repositories.PostRepository;
import main.repositories.UserRepository;
import main.services.interfaces.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements AuthService
{
    private ConcurrentHashMap<String, Integer> activeSessions = new ConcurrentHashMap<>();

    //@Value("${captcha.hour}")
    private LocalDateTime captchaLifetime;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CaptchaCodeRepository captchaCodeRepository;
    private final EmailSenderService emailSenderService;

    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           PostRepository postRepository,
                           CaptchaCodeRepository captchaCodeRepository,
                           EmailSenderService emailSenderService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.captchaCodeRepository = captchaCodeRepository;
        this.emailSenderService = emailSenderService;
    }

    @Override
    public AuthResponseBody login (String email, String password)
    {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        SecurityContextHolder.getContext().setAuthentication(auth);
        User user = (User) auth.getPrincipal();
        main.model.User currentUser = userRepository.findByEmail(user.getUsername());

        return AuthResponseBody.builder().result(true).user(getUserBody(currentUser, postRepository)).build();
    }

    @Override
    public AuthResponseBody checkAuth(Principal principal)
    {
        if (principal == null) {
            return getFalseResult();
        }
        main.model.User currentUser = userRepository.findByEmail(principal.getName());
        return AuthResponseBody.builder().result(true).user(getUserBody(currentUser, postRepository)).build();
    }

    @Override
    public AuthResponseBody logout()
    {
        activeSessions.remove(getSession().getId());
        return getTrueResult();
    }

    @Override
    public AuthResponseBody restorePassword(String email) {

        main.model.User user = userRepository.findByEmail(email);
        if (user == null) {
            return AuthResponseBody.builder().result(false).build();
        }
        else {
            String hash = getRandomHash(45);
            String link =  "/login/change-password/" + hash;
            user.setCode(hash);
            userRepository.save(user);
            emailSenderService.sendMessage(email, "password recovery", link);
            return AuthResponseBody.builder().result(true).build();
        }
    }

    @Override
    public AuthResponseBody changePassword(AuthRequestBody requestBody) {
        return null;
    }

    @Override
    public AuthResponseBody signIn(AuthRequestBody requestBody) {
        return null;
    }

    @Override
    public AuthResponseBody getCaptcha() throws IOException {

        String secretCode = getRandomHash(22);
        String captchaCode = getRandomHash(4);
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

    public HttpSession getSession()
    {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession(true); // true == allow create
    }

    public boolean isUserAuthorize()
    {
        String sessionId = getSession().getId();
        return activeSessions.containsKey(sessionId);
    }

    public int getAuthorizedUserId()
    {
        String sessionId = getSession().getId();
        return activeSessions.get(sessionId);
    }

    private String getRandomHash(int n) {
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
}

