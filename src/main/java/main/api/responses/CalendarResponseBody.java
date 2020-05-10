package main.api.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.TreeMap;

@Data
@AllArgsConstructor
public class CalendarResponseBody
{
    ArrayList<Integer> years;
    TreeMap<String, Integer> posts;
}
