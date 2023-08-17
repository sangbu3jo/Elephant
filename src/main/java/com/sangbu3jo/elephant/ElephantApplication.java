package com.sangbu3jo.elephant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ElephantApplication {

  public static void main(String[] args) {
    SpringApplication.run(ElephantApplication.class, args);
  }

}
