package main.services;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.api.responses.ApiResponseBody;
import main.api.responses.AuthResponseBody;
import main.api.responses.bodies.ErrorsBody;
import main.model.CaptchaCode;
import main.model.enums.Errors;
import main.repositories.CaptchaCodeRepository;
import main.repositories.PostRepository;
import main.repositories.UserRepository;
import main.services.interfaces.AuthService;
import main.services.interfaces.UtilitiesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService
{
    private final BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final EmailSenderService emailSenderService;
    private final UtilitiesService utilitiesService;
    private final CaptchaCodeRepository captchaCodeRepository;

    @Value("${link.prefix.for.recovery.password}")
    private String linkPrefix;

    @Value("${subject.for.recovery.mail}")
    private String subject;

    @Value("${hash.length}")
    private int hashLength;

    @Value("${min.password.length}")
    private int minPasswordLength;

    @Value("${name.max.length}")
    private int nameMaxLength;

    @Override
    public ResponseEntity<AuthResponseBody> login (String email, String password) {
        main.model.User user = userRepository.findByEmail(email);
        if (user == null || !bcryptEncoder.matches(password, user.getPassword())) {
            log.info("User - {} not find or password - {} is wrong", email, password);
            return ResponseEntity.ok(getFalseResult());
        }

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.info("User {} was successfully logged", email);

        return ResponseEntity.ok(AuthResponseBody.builder()
                .result(true)
                .user(getUserBody(user, postRepository))
                .build());
    }

    @Override
    public ResponseEntity<AuthResponseBody> checkAuth(Principal principal)
    {
           if (principal == null) {
               return ResponseEntity.ok(getFalseResult());
        }
        main.model.User currentUser = userRepository.findByEmail(principal.getName());
        return ResponseEntity.ok(AuthResponseBody.builder()
                .result(true)
                .user(getUserBody(currentUser, postRepository))
                .build());
    }

    @Override
    public ResponseEntity<AuthResponseBody> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(getTrueResult());
    }

    @Override
    public ResponseEntity<AuthResponseBody> restorePassword(String email, HttpServletRequest request) {

        main.model.User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.ok(getFalseResult());
        }
        else {
            String hash = utilitiesService.getRandomHash(hashLength);
            String hostName = request.getScheme() + "://" + request.getServerName() + ":"  + request.getServerPort();
            String recoveryLink = hostName + linkPrefix + hash;
            user.setCode(hash);
            userRepository.save(user);
            emailSenderService.sendMessage(email, subject, recoveryLink);
            log.info("Email {} was successfully sent with link: {}", email, recoveryLink);
            return ResponseEntity.ok(getTrueResult());
        }
    }

    @Override
    public ResponseEntity<ApiResponseBody> changePassword(String code, String password, String captcha,
                                                          String captchaSecret)
    {
        main.model.User user = userRepository.findByCode(code);
        CaptchaCode captchaCode = captchaCodeRepository.findByCode(encodeCaptcha(captcha));
        if (user == null) {
            return ResponseEntity.ok(ApiResponseBody.builder()
                    .result(false)
                    .errors(ErrorsBody.builder()
                            .code(Errors.CODE_IS_OUT_OF_DATE.getTitle())
                            .build())
                    .build());
        }

        if (captchaCode == null || !captchaCode.getSecretCode().equals(captchaSecret)) {
            return ResponseEntity.ok(ApiResponseBody.builder()
                    .result(false)
                    .errors(ErrorsBody.builder()
                            .captcha(Errors.CAPTCHA_IS_INCORRECT.getTitle())
                            .build())
                    .build());
        }

        if (password.length() < minPasswordLength) {
            return ResponseEntity.ok(ApiResponseBody.builder()
                    .result(false)
                    .errors(ErrorsBody.builder()
                            .captcha(Errors.PASSWORD_IS_SHORT.getTitle())
                            .build())
                    .build());
        }
        user.setPassword(bcryptEncoder.encode(password));
        user.setCode(null);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponseBody.builder().result(true).build());
    }

    @Override
    public ResponseEntity<ApiResponseBody> signIn(String email, String password, String name, String captcha,
                                                  String captchaSecret) {

        main.model.User user = userRepository.findByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(ApiResponseBody.builder()
                    .result(false)
                    .errors(ErrorsBody.builder()
                            .email(Errors.THIS_EMAIL_IS_EXIST.getTitle())
                            .build())
                    .build());
        }
        if (password.length() < minPasswordLength) {
            return ResponseEntity.ok(ApiResponseBody.builder()
                    .result(false)
                    .errors(ErrorsBody.builder()
                            .captcha(Errors.PASSWORD_IS_SHORT.getTitle())
                            .build())
                    .build());
        }
        if (name.length() > nameMaxLength || !name.matches("[aA-zZаА-яЯ0-9_\\- ]+"))
        {
            return ResponseEntity.ok(ApiResponseBody.builder()
                    .result(false)
                    .errors(ErrorsBody.builder()
                            .captcha(Errors.NAME_IS_INCORRECT.getTitle())
                            .build())
                    .build());
        }
        CaptchaCode captchaCode = captchaCodeRepository.findByCode(encodeCaptcha(captcha));
        if (captchaCode == null || !captchaCode.getSecretCode().equals(captchaSecret)) {
            return ResponseEntity.ok(ApiResponseBody.builder()
                    .result(false)
                    .errors(ErrorsBody.builder()
                            .captcha(Errors.CAPTCHA_IS_INCORRECT.getTitle())
                            .build())
                    .build());
        }

        userRepository.save(main.model.User.builder()
                .email(email)
                .name(name)
                .password(bcryptEncoder.encode(password))
                .isModerator((byte)0)
                .regTime(utilitiesService.getTime())
                .build());

        return ResponseEntity.ok(ApiResponseBody.builder().result(true).build());
    }

    private String getLoggedUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getUsername();
    }

    public boolean isUserAuthorize() {
        return userRepository.findByEmail(getLoggedUserName()) != null;
    }

    public main.model.User getAuthorizedUser() {
        return userRepository.findByEmail(getLoggedUserName());
    }

    private String encodeCaptcha(String captcha) {
        return Base64.getEncoder().encodeToString(captcha.getBytes());
    }
}

