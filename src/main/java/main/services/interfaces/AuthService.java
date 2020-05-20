package main.services.interfaces;

import main.api.responses.AuthResponseBody;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repositories.PostRepository;
import main.services.bodies.UserBody;

import javax.servlet.http.HttpSession;

public interface AuthService
{
    AuthResponseBody login(String email, String password);
    AuthResponseBody checkAuth();
    AuthResponseBody logout();
    AuthResponseBody restorePassword(String email);
    AuthResponseBody changePassword(String code, String password, String captcha, String captcha_secret);
    AuthResponseBody signIn(String email, String name, String password, String captcha, String captcha_secret);

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
        int moderationCount = postRepository.getTotalNewAndActivePosts((byte) 1, ModerationStatus.NEW);

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
