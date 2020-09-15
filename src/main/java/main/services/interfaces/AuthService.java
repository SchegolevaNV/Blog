package main.services.interfaces;

import main.api.requests.AuthRequestBody;
import main.api.responses.AuthResponseBody;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repositories.PostRepository;
import main.services.bodies.UserBody;

import javax.servlet.http.HttpSession;
import java.io.IOException;

public interface AuthService
{
    AuthResponseBody login(String email, String password);
    AuthResponseBody checkAuth();
    AuthResponseBody logout();
    AuthResponseBody restorePassword(String email);
    AuthResponseBody changePassword(AuthRequestBody requestBody);
    AuthResponseBody signIn(AuthRequestBody requestBody);
    AuthResponseBody getCaptcha() throws IOException;

    HttpSession getSession();
    boolean isUserAuthorize();
    int getAuthorizedUserId();

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
