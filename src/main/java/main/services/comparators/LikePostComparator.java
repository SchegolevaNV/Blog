package main.services.comparators;

import main.model.Post;

import java.util.Comparator;

public class LikePostComparator implements Comparator<Post> {
    @Override
    public int compare(Post post1, Post post2) {
        return post2.getVotesCount("likes") - post1.getVotesCount("likes");
    }
}
