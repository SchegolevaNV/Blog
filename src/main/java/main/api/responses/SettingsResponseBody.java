package main.api.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SettingsResponseBody
{
    private boolean MULTIUSER_MODE;
    private boolean POST_PREMODERATION;
    private boolean STATISTICS_IS_PUBLIC;
}
