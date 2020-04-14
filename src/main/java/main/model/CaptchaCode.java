package main.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Date;

@Entity
@Table(name = "captcha_codes")
public class CaptchaCode
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @NotNull
    private Date time;

    @NotNull
    private String code;

    @NotNull
    private String secret_code;
}
