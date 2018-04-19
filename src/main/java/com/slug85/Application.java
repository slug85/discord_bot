package com.slug85;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource("classpath:spring.xml")
public class Application {

  private static final Logger log = LoggerFactory.getLogger(Application.class);
  private static Launcher launcher;

  @Autowired
  private void setConnector(Launcher c) {
    launcher = c;
  }

  public static void main(String[] args) {

    SpringApplication.run(Application.class, args);
    launcher.login();

  }
}