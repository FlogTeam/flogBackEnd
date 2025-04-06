package golf.flogbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FlogBackEndApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlogBackEndApplication.class, args);
    }

}
