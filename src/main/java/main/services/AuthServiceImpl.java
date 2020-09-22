package main.services;
import lombok.extern.slf4j.Slf4j;
import main.api.requests.AuthRequestBody;
import main.api.responses.AuthResponseBody;
import main.repositories.PostRepository;
import main.repositories.UserRepository;
import main.services.interfaces.AuthService;
import main.services.interfaces.UtilitiesService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class AuthServiceImpl implements AuthService
{
    private final BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final EmailSenderService emailSenderService;
    private final UtilitiesService utilitiesService;

    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           PostRepository postRepository,
                           EmailSenderService emailSenderService,
                           UtilitiesService utilitiesService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.emailSenderService = emailSenderService;
        this.utilitiesService = utilitiesService;
    }

    @Override
    public AuthResponseBody login (String email, String password)
    {
        main.model.User user = userRepository.findByEmail(email);
        if (user == null || !bcryptEncoder.matches(password, user.getPassword())) {
            log.info("User - {} not find or password - {} is wrong", email, password);
            return getFalseResult();
        }

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.info("User {} was successfully logged", email);

        return AuthResponseBody.builder().result(true).user(getUserBody(user, postRepository)).build();
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
    public AuthResponseBody logout() {
        SecurityContextHolder.clearContext();
        return getTrueResult();
    }

    @Override
    public AuthResponseBody restorePassword(String email) {

        main.model.User user = userRepository.findByEmail(email);
        if (user == null) {
            return AuthResponseBody.builder().result(false).build();
        }
        else {
            String hash = utilitiesService.getRandomHash(45);
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

