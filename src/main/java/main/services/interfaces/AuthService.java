package main.services.interfaces;

import main.api.requests.AuthRequestBody;
import main.api.responses.AuthResponseBody;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repositories.PostRepository;
import main.api.responses.bodies.UserBody;
import org.springframework.security.core.Authentication;

import java.security.Principal;

public interface AuthService
{
    AuthResponseBody login(String email, String password);
    AuthResponseBody checkAuth(Principal principal);
    AuthResponseBody logout();
    AuthResponseBody restorePassword(String email);
    AuthResponseBody changePassword(AuthRequestBody requestBody);
    AuthResponseBody signIn(AuthRequestBody requestBody);

    boolean isUserAuthorize();
    main.model.User getAuthorizedUser();

    /** default методы **/

    default AuthResponseBody getTrueResult() {
        return AuthResponseBody.builder().result(true).build();
    }

    default AuthResponseBody getFalseResult() {
        return AuthResponseBody.builder().result(false).build();
    }

    default UserBody getUserBody(User user, PostRepository postRepository)
    {
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
