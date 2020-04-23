package main.services.interfaces;

import main.api.responses.PostResponseBody;

public interface PostService
{
    PostResponseBody getAllPosts(int offset, int limit, String mode);
}
