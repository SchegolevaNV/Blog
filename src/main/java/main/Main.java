package main;
import main.model.PostVote;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableJpaRepositories
@EntityScan(basePackages = {"main/model"})
@ComponentScan(basePackages = {"main/controller", "main/services"})

public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);

        System.out.println(LocalDateTime.now());
    }
}
