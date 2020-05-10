package main.services;

import main.api.responses.CalendarResponseBody;
import main.api.responses.SettingsResponseBody;
import main.api.responses.TagsResponseBody;
import main.model.GlobalSettings;
import main.model.Post;
import main.model.Tag;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repositories.TagRepository;
import main.repositories.UserRepository;
import main.services.bodies.TagsBody;
import main.services.interfaces.GeneralService;
import main.services.interfaces.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class GeneralServiceImpl implements GeneralService, QueryService {

    @Autowired
    TagRepository tagRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    UserRepository userRepository;

    @Override
    public TagsResponseBody getTags(String query) {

        Query postsCount = entityManager.createQuery(QueryService.postsCount).setParameter("dateNow", LocalDateTime.now());
        Long count = (Long) postsCount.getSingleResult();

        List<TagsBody> tags = new ArrayList<>();

        if (query == null)
            query = "";

        List<Tag> tagList = tagRepository.findByNameStartingWith(query);

        for (Tag tag : tagList)
        {
            List<Post> posts = tag.getTagsPosts();

            posts.removeIf(post -> post.getIsActive() != 1
                    && post.getModerationStatus() != ModerationStatus.ACCEPTED
                    && !post.getTime().isBefore(LocalDateTime.now()));

            double weight = (double) posts.size() / (double) count;
            tags.add(new TagsBody(tag.getName(), weight));
        }

        return new TagsResponseBody(tags);
    }

    @Override
    public CalendarResponseBody getCalendar(String year) {

        //TODO переделать метод выборки с groupBy и только года

        if (year == null
                || !year.matches("[0-9]{4}")
                || Integer.parseInt(year) < 2015
                || Integer.parseInt(year) > LocalDate.now().getYear())
            year = Integer.toString(LocalDate.now().getYear());

        Query allDates = entityManager.createQuery("SELECT time " + postsSelect)
                .setParameter("dateNow", LocalDateTime.now());
        List<LocalDateTime> times = allDates.getResultList();
        times.sort(Comparator.reverseOrder());

        ArrayList<Integer> years = new ArrayList<>();
        TreeMap<String, Integer> posts = new TreeMap<>();

        for (LocalDateTime time : times) {

            int thisYear = time.getYear();
            if(!years.contains(thisYear))
                years.add(thisYear);

            if (Integer.toString(thisYear).equals(year)) {
                String date = time.toLocalDate().toString();
                if (!posts.containsKey(date)) {
                    int postCount = 1;
                    posts.put(date, postCount);
                } else posts.replace(date, posts.get(date) + 1);
            }
        }
        return new CalendarResponseBody(years, posts);
    }

    @Override
    public SettingsResponseBody getSettings()
    {
        String sessionId = AuthServiceImpl.getSession().getId();

        boolean MULTIUSER_MODE = false;
        boolean POST_PREMODERATION = false;
        boolean STATISTICS_IS_PUBLIC = false;

        if (AuthServiceImpl.activeSessions.containsKey(sessionId))
        {
            int userId = AuthServiceImpl.activeSessions.get(sessionId);
            User user = userRepository.findById(userId);

            if (user.getIsModerator() == 1)
            {
                Query settings = entityManager.createQuery("FROM GlobalSettings s");
                List<GlobalSettings> globalSettings = settings.getResultList();

                for (GlobalSettings mySettings : globalSettings)
                {
                    boolean value = false;

                    if (mySettings.getValue().equals("YES"))
                        value = true;

                    if (mySettings.getCode().equals("MULTIUSER_MODE"))
                        MULTIUSER_MODE = value;
                    if (mySettings.getCode().equals("POST_PREMODERATION"))
                        POST_PREMODERATION = value;
                    if (mySettings.getCode().equals("STATISTICS_IS_PUBLIC"))
                        STATISTICS_IS_PUBLIC = value;
                }
                return new SettingsResponseBody(MULTIUSER_MODE, POST_PREMODERATION, STATISTICS_IS_PUBLIC);
            }
        }
        return null;
    }

    @Override
    @Transactional
    public SettingsResponseBody putSettings(SettingsResponseBody settingsResponseBody)
    {
        String value = "NO";
        HashMap<String, Boolean> codes = new HashMap<>();
        codes.put("MULTIUSER_MODE", settingsResponseBody.isMULTIUSER_MODE());
        codes.put("POST_PREMODERATION", settingsResponseBody.isPOST_PREMODERATION());
        codes.put("STATISTICS_IS_PUBLIC", settingsResponseBody.isSTATISTICS_IS_PUBLIC());

        String sessionId = AuthServiceImpl.getSession().getId();
        if (AuthServiceImpl.activeSessions.containsKey(sessionId)) {
            int userId = AuthServiceImpl.activeSessions.get(sessionId);
            User user = userRepository.findById(userId);

            if (user.getIsModerator() == 1) {
                for (Map.Entry<String, Boolean> code : codes.entrySet()) {
                    if (code.getValue())
                        value = "YES";
                    Query updateSettings = entityManager.createQuery("UPDATE GlobalSettings SET value = :value WHERE code = :code");
                    updateSettings.setParameter("code", code.getKey());
                    updateSettings.setParameter("value", value);
                }
            }
            return settingsResponseBody;
        }
        return null;
    }

//    @Override
//    @Transactional
//    public SettingsResponseBody putSettings(boolean MULTIUSER_MODE, boolean POST_PREMODERATION, boolean STATISTICS_IS_PUBLIC)
//    {
//        String value = "NO";
//        HashMap<String, Boolean> codes = new HashMap<>();
//        codes.put("MULTIUSER_MODE", MULTIUSER_MODE);
//        codes.put("POST_PREMODERATION", POST_PREMODERATION);
//        codes.put("STATISTICS_IS_PUBLIC", STATISTICS_IS_PUBLIC);
//
//        String sessionId = AuthServiceImpl.getSession().getId();
//        if (AuthServiceImpl.activeSessions.containsKey(sessionId)) {
//            int userId = AuthServiceImpl.activeSessions.get(sessionId);
//            User user = userRepository.findById(userId);
//
//            if (user.getIsModerator() == 1) {
//                for (Map.Entry<String, Boolean> code : codes.entrySet()) {
//                    if (code.getValue())
//                        value = "YES";
//                    Query updateSettings = entityManager.createQuery("UPDATE GlobalSettings SET value = :value WHERE code = :code");
//                    updateSettings.setParameter("code", code.getKey());
//                    updateSettings.setParameter("value", value);
//                }
//                return new SettingsResponseBody(MULTIUSER_MODE, POST_PREMODERATION, STATISTICS_IS_PUBLIC);
//            }
//        }
//        return null;
//    }
}
