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
    ResponseEntity<TagsResponseBody> getTags(String query);
    ResponseEntity<CalendarResponseBody> getCalendar(String year);
    ResponseEntity<SettingsResponseBody> getSettings();
    ResponseEntity<SettingsResponseBody> putSettings(boolean MULTIUSER_MODE, boolean POST_PREMODERATION, boolean STATISTICS_IS_PUBLIC);
    ResponseEntity<StatisticResponseBody> getMyStatistics();
    ResponseEntity<StatisticResponseBody> getAllStatistics();
    ResponseEntity<ApiResponseBody> addComment(ApiRequestBody comment);
    ResponseEntity<ApiResponseBody> moderation(ApiRequestBody requestBody);
    ResponseEntity<ApiResponseBody> editProfile();
    ResponseEntity imageUpload(MultipartFile file) throws IOException;

    default StatisticResponseBody createStatisticResponseBody(List<Post> posts, UtilitiesService utilitiesService)
    {
        int likesCount = 0;
        int dislikesCount = 0;
        int viewsCount = 0;

        Long firstPublication = utilitiesService.getTimestampFromLocalDateTime(posts.get(0).getTime());
        int postsCount = posts.size();

        for (Post post : posts) {
            viewsCount += post.getViewCount();
            likesCount += post.getVotesCount("likes");
            dislikesCount += post.getVotesCount("dislikes");
        }
        return new StatisticResponseBody(postsCount, likesCount, dislikesCount, viewsCount, firstPublication);
    }
}
