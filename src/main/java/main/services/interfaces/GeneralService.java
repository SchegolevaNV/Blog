package main.services.interfaces;

import main.api.requests.ApiRequestBody;
import main.api.responses.*;
import main.model.Post;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

public interface GeneralService
{
    ResponseEntity<TagsResponseBody> getTags(String query);
    ResponseEntity<CalendarResponseBody> getCalendar(String year);
    ResponseEntity<SettingsResponseBody> getSettings();
    ResponseEntity<SettingsResponseBody> putSettings(boolean multiuserMode, boolean postPremoderation, boolean statisticsIsPublic);
    ResponseEntity<StatisticResponseBody> getMyStatistics();
    ResponseEntity<StatisticResponseBody> getAllStatistics(Principal principal);
    ResponseEntity<ApiResponseBody> addComment(ApiRequestBody comment);
    ResponseEntity<ApiResponseBody> moderation(int postId, String decision);
    ResponseEntity<ApiResponseBody> editProfileWithPhoto(String email, int removePhoto, MultipartFile file,
                                                         String name,String password) throws IOException;
    ResponseEntity<ApiResponseBody> editProfileWithoutPhoto(ApiRequestBody apiRequestBody);
    ResponseEntity<Object> imageUpload(MultipartFile file) throws IOException;

    default StatisticResponseBody createStatisticResponseBody(List<Post> posts, UtilitiesService utilitiesService)
    {
        int likesCount = 0;
        int dislikesCount = 0;
        int viewsCount = 0;

        long firstPublication = utilitiesService.getTimestampFromLocalDateTime(posts.get(0).getTime());
        int postsCount = posts.size();

        for (Post post : posts) {
            viewsCount += post.getViewCount();
            likesCount += post.getVotesCount("likes");
            dislikesCount += post.getVotesCount("dislikes");
        }
        return new StatisticResponseBody(postsCount, likesCount, dislikesCount, viewsCount, firstPublication);
    }
}
