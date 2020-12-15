package main;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.TimeZone;

@Slf4j
@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
@PropertySource("classpath:constants.yml")
@EntityScan(basePackages = {"main/model"})
public class Main {

    @Value("${storage.location}")
    private String location;

    public static void main(String[] args) {

        SpringApplication.run(Main.class, args);
        System.out.println("I'm ready");
    }
    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        File upload = new File(location);
        if (upload.mkdir())
            log.info("Directory {} was successfully created", location);
        else log.info("The {} directory already exist", location);
    }
}