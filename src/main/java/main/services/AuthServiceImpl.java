package main.services;

import main.api.requests.AuthRequestBody;
import main.api.responses.AuthResponseBody;
import main.model.User;
import main.repositories.PostRepository;
import main.repositories.UserRepository;
import main.services.interfaces.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements AuthService
{
    private ConcurrentHashMap<String, Integer> activeSessions = new ConcurrentHashMap<>();

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

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

        return AuthResponseBody.builder().result(true).user(getUserBody(user, postRepository)).build();
    }

    @Override
    public AuthResponseBody checkAuth()
    {
        String sessionId = getSession().getId();

        if (activeSessions.containsKey(sessionId))
        {
            int userId = activeSessions.get(sessionId);
            User user = userRepository.findById(userId);

            return AuthResponseBody.builder().result(true).user(getUserBody(user, postRepository)).build();
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
    public AuthResponseBody changePassword(AuthRequestBody requestBody) {
        return null;
    }

    @Override
    public AuthResponseBody signIn(AuthRequestBody requestBody) {
        return null;
    }

    public HttpSession getSession()
    {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession(true); // true == allow create
    }

    public boolean isUserAuthorize()
    {
        String sessionId = getSession().getId();
        return activeSessions.containsKey(sessionId);
    }

    public int getAuthorizedUserId()
    {
        String sessionId = getSession().getId();
        return activeSessions.get(sessionId);
    }
}

