package main.services.comparators;

import main.model.Post;

import java.util.Comparator;

public class CommentPostComparator implements Comparator<Post> {
    @Override
    public int compare(Post post1, Post post2) {
        return post1.getPostComments().size() - post2.getPostComments().size();
    }
}
