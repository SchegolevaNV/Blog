package main.api.responses;

import java.util.List;
import java.util.TreeMap;

public record CalendarResponseBody(List<Integer> years, TreeMap<String, Long> posts) {
}
