package main.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Date;

@Entity
@Table(name = "post_comments")
public class PostComment
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private int parent_id;

    @NotNull
    private int post_id;

    @NotNull
    private int user_id;

    @NotNull
    private Date time;

    @NotNull
    private String text;
}
