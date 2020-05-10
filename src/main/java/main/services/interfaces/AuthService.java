package main.services.interfaces;

import main.api.responses.AuthResponseBody;
import main.model.User;
import main.services.bodies.UserBody;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.concurrent.ConcurrentHashMap;

public interface AuthService
{
    ConcurrentHashMap<String, Integer> activeSessions = new ConcurrentHashMap<>();

    AuthResponseBody login(String email, String password);
    AuthResponseBody checkAuth();
    AuthResponseBody logout();
    AuthResponseBody restorePassword(String email);
    AuthResponseBody changePassword(String code, String password, String captcha, String captcha_secret);
    AuthResponseBody signIn(String email, String name, String password, String captcha, String captcha_secret);

    /** default методы **/

    default AuthResponseBody getTrueResult() {
        return AuthResponseBody.builder().result(true).build();
    }

    default AuthResponseBody getFalseResult() {
        return AuthResponseBody.builder().result(false).build();
    }

    default UserBody getUserBody(User user, EntityManager entityManager)
    {
        boolean moderationStatus = false;
        long moderationCount = 0;

        if (user.getIsModerator() == 1) {
            Query query = entityManager.createQuery("SELECT COUNT(*) FROM Post p WHERE p.moderatorId = " + user.getId());
            moderationStatus = true;
            moderationCount = (long) query.getSingleResult();
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
