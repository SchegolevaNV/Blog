package main.model;

import lombok.*;
import main.model.enums.ModerationStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "posts")
public class Post
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Column(name = "is_active")
    private byte isActive;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "moderation_status", columnDefinition = "DEFAULT 'NEW'")
    private ModerationStatus moderationStatus;

    @Column(name = "moderator_id")
    private Integer moderatorId;

    @NotNull
    @ManyToOne(cascade = CascadeType.ALL)
    private User user;

    @NonNull
    private LocalDateTime time;

    @NotNull
    private String title;

    @NotNull
    @Column(columnDefinition = "TEXT")
    private String text;

    @NotNull
    @Column(name = "view_count")
    private int viewCount;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "tag2post",
            joinColumns = {@JoinColumn(name = "post_id")},
            inverseJoinColumns = {@JoinColumn(name = "tag_id")})
    private List<Tag> postTags;

    @OneToMany(mappedBy = "post")
    private List<PostVote> postVote;

    @OneToMany(mappedBy = "post")
    private List<PostComment> postComments;

    public int getCommentsCount()
    {
        return getPostComments().size();
    }

    public int getVotesCount(String value)
    {
        List<PostVote> postVotes = getPostVote();

        int likeCounts = 0;
        int dislikeCount = 0;
        for (PostVote postVote : postVotes)
        {
            if (postVote.getValue() == 1)
                likeCounts++;
            else dislikeCount++;
        }
        return (value.equals("likes")) ? likeCounts : dislikeCount;
    }
}
