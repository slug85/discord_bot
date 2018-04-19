package com.slug85;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

@SpringBootApplication
public class Application {

  private static final Logger log = LoggerFactory.getLogger(Application.class);
  private static CommandHandler commandHandler;

  @Autowired
  private void setCommandHandler(CommandHandler ch) {
    commandHandler = ch;
  }

  private static String clientToken;

  @Value("${discord.CLIENT_TOKEN}")
  private void setClientToken(String ct) {
    clientToken = ct;
  }

  public static void main(String[] args) {

    SpringApplication.run(Application.class, args);

    try {
      IDiscordClient cli = new ClientBuilder().withToken(clientToken).withRecommendedShardCount().build();

      log.info(cli.getApplicationName() + " has started");

      cli.getDispatcher().registerListener(commandHandler);
      cli.login();

    } catch (Exception e) {
      // do nothing.
      log.warn("WARNING - Discord4J :" + e.getMessage());
    }
  }
}