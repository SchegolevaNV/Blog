package main.services;

import main.api.responses.AuthResponseBody;
import main.model.User;
import main.repositories.UserRepository;
import main.services.interfaces.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpSession;

@Service
public class AuthServiceImpl implements AuthService
{
    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    UserRepository userRepository;

    @Override
    public AuthResponseBody login (String email, String password)
    {
        User user = userRepository.findByEmail(email);

        if (user != null && user.getPassword().equals(password))
        {
            HttpSession session = getSession();
            activeSessions.put(session.getId(), user.getId());
        }
        else return getFalseResult();

        return AuthResponseBody.builder().result(true).user(getUserBody(user, entityManager)).build();
    }

    @Override
    public AuthResponseBody checkAuth()
    {
        String sessionId = getSession().getId();

        if (activeSessions.containsKey(sessionId))
        {
            int userId = activeSessions.get(sessionId);
            User user = userRepository.findById(userId);

            return AuthResponseBody.builder().result(true).user(getUserBody(user, entityManager)).build();
        }
        else
            return getFalseResult();
    }

    @Override
    public AuthResponseBody logout()
    {
        activeSessions.remove(getSession().getId());
        return getTrueResult();
    }

    @Override
    public AuthResponseBody restorePassword(String email) {
        return null;
    }

    @Override
    public AuthResponseBody changePassword(String code, String password, String captcha, String captcha_secret) {
        return null;
    }

    @Override
    public AuthResponseBody signIn(String email, String name, String password, String captcha, String captcha_secret) {
        return null;
    }

    public static HttpSession getSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession(true); // true == allow create
    }
}

