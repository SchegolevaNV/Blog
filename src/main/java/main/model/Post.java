package main.model;

import main.model.enums.ModerationStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Date;

@Entity
@Table(name = "posts")
public class Post
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull
    private byte is_active;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(columnDefinition = "moderationStatus DEFAULT 'NEW'")
    private ModerationStatus moderation_status;

    private int moderator_id;

    @NotNull
    private int user_id;

    @NotNull
    private Date time;

    @NotNull
    private String title;

    @NotNull
    private String text;

    @NotNull
    private int view_count;
}
