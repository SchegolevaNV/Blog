package main.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Date;

@Entity
@Table(name = "post_votes")
public class PostVote
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull
    private int user_id;

    @NotNull
    private int post_id;

    @NotNull
    private Date time;

    @NotNull
    private byte value;
}
