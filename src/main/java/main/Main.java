package main;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
@PropertySource("classpath:constants.yml")
@EntityScan(basePackages = {"main/model"})
public class Main {

    public static void main(String[] args) {

        SpringApplication.run(Main.class, args);
        System.out.println("I'm ready");
    }
}