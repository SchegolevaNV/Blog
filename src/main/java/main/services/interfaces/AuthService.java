package main.services.interfaces;

import main.api.responses.AuthResponseBody;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repositories.PostRepository;
import main.api.responses.bodies.UserBody;
import org.springframework.http.ResponseEntity;

import java.security.Principal;

public interface AuthService
{
    ResponseEntity<AuthResponseBody> login(String email, String password);
    ResponseEntity<AuthResponseBody> checkAuth(Principal principal);
    ResponseEntity<AuthResponseBody> logout();
    ResponseEntity<AuthResponseBody> restorePassword(String email);
    ResponseEntity<AuthResponseBody> changePassword(String code, String password, String captcha, String captchaSecret);
    ResponseEntity<AuthResponseBody> signIn(String email, String password, String name, String captcha, String captchaSecret);

    boolean isUserAuthorize();
    main.model.User getAuthorizedUser();

    /** default методы **/

    default AuthResponseBody getTrueResult() {
        return AuthResponseBody.builder().result(true).build();
    }
    default AuthResponseBody getFalseResult() {
        return AuthResponseBody.builder().result(false).build();
    }

    default UserBody getUserBody(User user, PostRepository postRepository) {
        boolean moderationStatus = false;
        int moderationCount = postRepository.getPostsCountByActiveAndModStatus((byte) 1, ModerationStatus.NEW);

        if (user.getIsModerator() == 1) {
            moderationStatus = true;
        }

        return UserBody.builder().id(user.getId())
                .name(user.getName())
                .photo(user.getPhoto())
                .email(user.getEmail())
                .moderation(moderationStatus)
                .moderationCount(moderationCount)
                .settings(moderationStatus).build();
    }
}
