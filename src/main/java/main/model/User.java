package main.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Date;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull
    private byte is_moderator;

    @NotNull
    private Date reg_time;

    @NotNull
    private String name;

    @NotNull
    private String email;

    @NotNull
    private String password;

    private String code;
    private String text;
}
