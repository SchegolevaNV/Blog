package main.services.interfaces;

import main.api.requests.ApiRequestBody;
import main.api.responses.*;
import main.model.Post;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface GeneralService
{
    TagsResponseBody getTags(String query);
    CalendarResponseBody getCalendar(String year);
    SettingsResponseBody getSettings();
    SettingsResponseBody putSettings(boolean MULTIUSER_MODE, boolean POST_PREMODERATION, boolean STATISTICS_IS_PUBLIC);
    ResponseEntity<StatisticResponseBody> getMyStatistics();
    ResponseEntity<StatisticResponseBody> getAllStatistics();
    ResponseEntity<ApiResponseBody> addComment(ApiRequestBody comment);
    ResponseEntity<ApiResponseBody> moderation(ApiRequestBody requestBody);
    ApiResponseBody editProfile();
    ResponseEntity imageUpload(MultipartFile file) throws IOException;

    default StatisticResponseBody createStatisticResponseBody(List<Post> posts)
    {
        int likesCount = 0;
        int dislikesCount = 0;
        int viewsCount = 0;

        String firstPublication = posts.get(0).getTime().format(PostService.formatter);
        int postsCount = posts.size();

        for (Post post : posts) {
            viewsCount += post.getViewCount();
            likesCount += post.getVotesCount("likes");
            dislikesCount += post.getVotesCount("dislikes");
        }
        return new StatisticResponseBody(postsCount, likesCount, dislikesCount, viewsCount, firstPublication);
    }
}
