package main.api.responses;

import lombok.Data;

@Data
public class StatisticResponseBody
{
    private  int postsCount;
    private int likesCount;
    private int dislikesCount;
    private int viewsCount;
    private String firstPublication;
}
