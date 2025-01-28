package main.controller;

import lombok.RequiredArgsConstructor;
import main.api.requests.ApiRequestBody;
import main.api.responses.*;
import main.services.interfaces.GeneralService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import main.configuration.BlogConfig;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
public class ApiGeneralController {

    private final GeneralService generalService;

    @GetMapping("init")
    public BlogConfig.Blog getBlogInfo()
    {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BlogConfig.class);
        BlogConfig.Blog blog = context.getBean(BlogConfig.Blog.class);
        context.close();

        return blog;
    }

    @GetMapping("calendar")
    public ResponseEntity<CalendarResponseBody> getCalendar(String year) {
        return generalService.getCalendar(year);
    }

    @GetMapping("tag")
    public ResponseEntity<TagsResponseBody> getTags(@RequestParam (required = false) String query) {
        return generalService.getTags(query);
    }

    @GetMapping("settings")
    public ResponseEntity<SettingsResponseBody> getSettings()
    {
        return generalService.getSettings();
    }

    @PutMapping ("settings")
    @PreAuthorize("hasAuthority('user:moderator')")
    public ResponseEntity<SettingsResponseBody> putSettings(@RequestBody SettingsResponseBody settings)
    {
        return generalService.putSettings(settings.isMultiuserMode(), settings.isPostPremoderation(),
                settings.isStatisticsIsPublic());
    }

    @GetMapping("statistics/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<StatisticResponseBody> getMyStatistics()
    {
        return generalService.getMyStatistics();
    }

    @GetMapping("statistics/all")
    public ResponseEntity<StatisticResponseBody> getAllStatistics(Principal principal)
    {
        return generalService.getAllStatistics(principal);
    }

    @PostMapping("comment")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ApiResponseBody> addComment(@RequestBody ApiRequestBody comment)
    {
        return generalService.addComment(comment);
    }

    @PostMapping("moderation")
    @PreAuthorize("hasAuthority('user:moderator')")
    public ResponseEntity<ApiResponseBody> moderation(@RequestBody ApiRequestBody requestBody)
    {
        return generalService.moderation(requestBody.postId(), requestBody.decision());
    }

    @PostMapping(value = "image", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<Object> imageUpload(@RequestPart(value = "image") MultipartFile file) throws IOException {
        return generalService.imageUpload(file);
    }

    @PostMapping(value = "/profile/my", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ApiResponseBody> editProfile(
            @RequestParam(value = "email") String email,
            @RequestParam(value = "removePhoto") int removePhoto,
            @RequestParam(value = "photo") MultipartFile file,
            @RequestParam(value = "name") String name,
            @RequestParam(value = "password", required = false) String password
    ) throws IOException {
        return generalService.editProfileWithPhoto(email, removePhoto, file, name, password);
    }

    @PostMapping("/profile/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ApiResponseBody> editProfile(@RequestBody ApiRequestBody apiRequestBody) {
        return generalService.editProfileWithoutPhoto(apiRequestBody);
    }
}
