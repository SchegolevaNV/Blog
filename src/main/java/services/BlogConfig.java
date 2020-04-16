package services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:blog.properties")
public class BlogConfig {

    @Autowired
    Environment env;

    @Bean
    public Blog get() {
        return new Blog(env.getProperty("blog.title"),
                        env.getProperty("blog.subtitle"),
                        env.getProperty("blog.phone"),
                        env.getProperty("blog.email"),
                        env.getProperty("blog.copyright"),
                        env.getProperty("blog.copyrightFrom"));
    }
}
