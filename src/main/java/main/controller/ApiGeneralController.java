package main.controller;

import main.api.requests.ApiRequestBody;
import main.api.responses.*;
import main.services.interfaces.GeneralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import main.configuration.Blog;
import main.configuration.Config;

@RestController
@RequestMapping("/api/")
public class ApiGeneralController
{
    @Autowired
    GeneralService generalService;

    @GetMapping("init")
    public Blog getBlogInfo()
    {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
        Blog blog = context.getBean(Blog.class);
        context.close();

        return blog;
    }

    @GetMapping("calendar")
    public CalendarResponseBody getCalendar(String year)
    {
        return generalService.getCalendar(year);
    }

    @GetMapping("tag")
   // @ResponseBody
    public TagsResponseBody getTags(@RequestParam (required = false) String query)
    {
        return generalService.getTags(query);
    }

    @GetMapping("settings")
    public SettingsResponseBody getSettings()
    {
        return generalService.getSettings();
    }

    @PutMapping ("settings")
    public SettingsResponseBody putSettings(@RequestBody SettingsResponseBody settings)
    {
        return generalService.putSettings(settings.isMULTIUSER_MODE(), settings.isPOST_PREMODERATION(), settings.isSTATISTICS_IS_PUBLIC());
    }

    @GetMapping("statistics/my")
    public StatisticResponseBody getMyStatistics()
    {
        return generalService.getMyStatistics();
    }

    @GetMapping("statistics/all")
    public ResponseEntity<StatisticResponseBody> getAllStatistics()
    {
        return generalService.getAllStatistics();
    }

    @PostMapping("comment")
    public ResponseEntity<ApiResponseBody> addComment(@RequestBody ApiRequestBody comment)
    {
        return generalService.addComment(comment);
    }
}
