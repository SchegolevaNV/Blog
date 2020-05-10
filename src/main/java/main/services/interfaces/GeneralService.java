package main.services.interfaces;

import main.api.responses.CalendarResponseBody;
import main.api.responses.SettingsResponseBody;
import main.api.responses.TagsResponseBody;

public interface GeneralService
{
    TagsResponseBody getTags(String query);
    CalendarResponseBody getCalendar(String year);
    SettingsResponseBody getSettings();
//    SettingsResponseBody putSettings(boolean MULTIUSER_MODE, boolean POST_PREMODERATION, boolean STATISTICS_IS_PUBLIC);
    SettingsResponseBody putSettings (SettingsResponseBody settingsResponseBody);
}
