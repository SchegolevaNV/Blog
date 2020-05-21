package main.api.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import java.util.TreeMap;

@Data
@AllArgsConstructor
public class CalendarResponseBody
{
    List<Integer> years;
    TreeMap<String, Long> posts;
}
