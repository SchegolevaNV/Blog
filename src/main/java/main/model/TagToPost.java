package main.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@Entity
@Builder
@Table(name = "tag2post")
public class TagToPost
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @Column(name = "post_id")
    private int postId;

    @NotNull
    @Column(name = "tag_id")
    private int tagId;
}
