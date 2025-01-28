package main.api.responses.bodies;

public record CommentBody(int id,
                          long time,
                          String text,
                          UserBody user) {
}