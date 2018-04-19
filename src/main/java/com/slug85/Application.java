package com.slug85;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

@SpringBootApplication
public class Application {

  private static final Logger log = LoggerFactory.getLogger(Application.class);


  private static String clientToken;

  @Value("${discord.CLIENT_TOKEN}")
  private void setClientToken(String ct) {
    clientToken = ct;
  }

  public static void main(String[] args) {

    SpringApplication.run(Application.class, args);

    try {

      IDiscordClient client = ApplicationContextProvider.getApplicationContext().getBean(IDiscordClient.class);
      CommandHandler commandHandler = ApplicationContextProvider.getApplicationContext().getBean(CommandHandler.class);

      log.info(client.getApplicationName() + " has started");

      client.getDispatcher().registerListener(commandHandler);
      client.login();

    } catch (Exception e) {
      // do nothing.
      log.warn("WARNING - Discord4J :" + e.getMessage());
    }
  }

  @Bean
  public IDiscordClient buildClient(){
    return new ClientBuilder().withToken(clientToken).withRecommendedShardCount().build();

  }
}