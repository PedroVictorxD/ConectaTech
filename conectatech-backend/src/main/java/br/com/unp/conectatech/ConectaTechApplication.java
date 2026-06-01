package br.com.unp.conectatech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ConectaTechApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConectaTechApplication.class, args);
    }
}
