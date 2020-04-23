package main.configuration;

import lombok.Data;
import lombok.Value;

@Data
@Value
public class Blog {

    String title;
    String subtitle;
    String phone;
    String email;
    String copyright;
    String copyrightFrom;
}
