package main.services.interfaces;

import main.api.responses.ApiResponseBody;
import main.api.responses.AuthResponseBody;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repositories.PostRepository;
import main.api.responses.bodies.UserBody;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;

public interface AuthService
{
    ResponseEntity<AuthResponseBody> login(HttpServletRequest req, String email, String password);
    ResponseEntity<AuthResponseBody> checkAuth(Principal principal);
    ResponseEntity<AuthResponseBody> logout();
    ResponseEntity<AuthResponseBody> restorePassword(String email, HttpServletRequest request);
    ResponseEntity<ApiResponseBody> changePassword(String code, String password, String captcha, String captchaSecret);
    ResponseEntity<ApiResponseBody> signIn(String email, String password, String name, String captcha, String captchaSecret);

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
        boolean moderationStatus = user.getIsModerator() == 1;
        int moderationCount = postRepository.getPostsCountByActiveAndModStatus((byte) 1, ModerationStatus.NEW);

        return UserBody.builder().id(user.getId())
                .name(user.getName())
                .photo(user.getPhoto())
                .email(user.getEmail())
                .moderation(moderationStatus)
                .moderationCount(moderationCount)
                .settings(moderationStatus).build();
    }
}
