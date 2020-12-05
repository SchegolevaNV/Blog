package main.services;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.api.responses.ApiResponseBody;
import main.api.responses.AuthResponseBody;
import main.api.responses.bodies.ErrorsBody;
import main.model.CaptchaCode;
import main.model.GlobalSettings;
import main.model.enums.Errors;
import main.repositories.CaptchaCodeRepository;
import main.repositories.GlobalSettingsRepository;
import main.repositories.PostRepository;
import main.repositories.UserRepository;
import main.services.interfaces.AuthService;
import main.services.interfaces.UtilitiesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Base64;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService
{
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final EmailSenderService emailSenderService;
    private final UtilitiesService utilitiesService;
    private final CaptchaCodeRepository captchaCodeRepository;
    private final GlobalSettingsRepository globalSettingsRepository;

    @Value("${link.prefix.for.recovery.password}")
    private String linkPrefix;

    @Value("${subject.for.recovery.mail}")
    private String subject;

    @Value("${hash.length}")
    private int hashLength;

    @Override
    public ResponseEntity<AuthResponseBody> login (String email, String password) {
        main.model.User user = userRepository.findByEmail(email);
        if (user == null || !utilitiesService.isUserTypeCorrectPassword(password, user.getPassword())) {
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
    public ResponseEntity<AuthResponseBody> checkAuth(Principal principal) {
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

        Optional<main.model.User> user = Optional.ofNullable(userRepository.findByEmail(email));
        if (user.isEmpty()) {
            return ResponseEntity.ok(getFalseResult());
        }
        else {
            String hash = utilitiesService.getRandomHash(hashLength);
            String hostName = request.getScheme() + "://" + request.getServerName() + ":"  + request.getServerPort();
            String recoveryLink = hostName + linkPrefix + hash;
            user.get().setCode(hash);
            userRepository.save(user.get());
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
            return ResponseEntity.ok(utilitiesService.getErrorResponse(ErrorsBody.builder()
                            .code(Errors.CODE_IS_OUT_OF_DATE.getTitle())
                            .build()));
        }
        if (captchaCode == null || !captchaCode.getSecretCode().equals(captchaSecret)) {
            return ResponseEntity.ok(getIncorrectCaptchaErrorResponse());
        }

        if (!utilitiesService.isPasswordNotShort(password)) {
            return ResponseEntity.ok(utilitiesService.getShortPasswordErrorResponse());
        }
        user.setPassword(utilitiesService.encodePassword(password));
        user.setCode(null);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponseBody.builder().result(true).build());
    }

    @Override
    public ResponseEntity<ApiResponseBody> signIn(String email, String password, String name, String captcha,
                                                  String captchaSecret) {

        GlobalSettings multiuserMode = globalSettingsRepository.findByCode("MULTIUSER_MODE");
        if (multiuserMode.getValue().equals("NO"))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        main.model.User user = userRepository.findByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(utilitiesService.getErrorResponse(ErrorsBody.builder()
                            .email(Errors.THIS_EMAIL_IS_EXIST.getTitle())
                            .build()));
        }
        if (!utilitiesService.isEmailCorrect(email))
            return ResponseEntity.ok(utilitiesService.getIncorrectEmailErrorResponse());

        if (!utilitiesService.isPasswordNotShort(password))
            return ResponseEntity.ok(utilitiesService.getShortPasswordErrorResponse());

        if (!utilitiesService.isNameCorrect(name))
            return ResponseEntity.ok(utilitiesService.getIncorrectNameErrorResponse());

        CaptchaCode captchaCode = captchaCodeRepository.findByCode(encodeCaptcha(captcha));
        if (captchaCode == null || !captchaCode.getSecretCode().equals(captchaSecret)) {
            return ResponseEntity.ok(getIncorrectCaptchaErrorResponse());
        }
        userRepository.save(main.model.User.builder()
                .email(email)
                .name(name)
                .password(utilitiesService.encodePassword(password))
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

    private ApiResponseBody getIncorrectCaptchaErrorResponse() {
        return ApiResponseBody.builder()
                .result(false)
                .errors(ErrorsBody.builder()
                        .captcha(Errors.CAPTCHA_IS_INCORRECT.getTitle())
                        .build())
                .build();
    }
}

