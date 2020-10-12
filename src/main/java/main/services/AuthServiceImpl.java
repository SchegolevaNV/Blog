package main.services;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import main.api.responses.AuthResponseBody;
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
import java.security.Principal;

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

    @Value("${link.for.recovery.password}")
    private String link;

    @Value("${subject.for.recovery.mail}")
    private String subject;

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
    public ResponseEntity<AuthResponseBody> restorePassword(String email) {

        main.model.User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.ok(getFalseResult());
        }
        else {
            String hash = utilitiesService.getRandomHash(45);
            link += hash;
            user.setCode(hash);
            userRepository.save(user);
            emailSenderService.sendMessage(email, subject, link);
            return ResponseEntity.ok(getTrueResult());
        }
    }

    @Override
    public ResponseEntity<AuthResponseBody> changePassword(String code, String password, String captcha, String captchaSecret)
    {
        //code - из таблицы юзеров
        //captcha - поле код в таблице капчи
        //secret - поле секрет в таблице капчи
        //капча может устареть!

        return null;
    }

    @Override
    public ResponseEntity<AuthResponseBody> signIn(String email, String password, String name, String captcha, String captchaSecret) {
        return null;
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
}

