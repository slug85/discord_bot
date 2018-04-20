package com.slug85;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

/**
 * Created by sergey.lugovskoi on 19.04.2018.
 */
@Component
public class Launcher {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private String clientToken;
    private CommandHandler commandHandler;
    private IDiscordClient client;

    @Value("${discord.CLIENT_TOKEN}")
    private void setClientToken(String ct) {
        clientToken = ct;
    }

    @Autowired
    private void setCommandHandler(CommandHandler ch) {
        commandHandler = ch;
    }

    void login(){
        LOGGER.info("start login process");
        IDiscordClient cli = new ClientBuilder().withToken(clientToken).withRecommendedShardCount().build();
        LOGGER.info(cli.getApplicationName() + " has started");
        cli.getDispatcher().registerListener(clientToken);
        cli.login();
        addListeners(cli);
        this.client = cli;
    }

    public IDiscordClient getClient() {
        return client;
    }

    public void setClient(IDiscordClient client) {
        this.client = client;
    }

    private void addListeners(IDiscordClient cli){
        cli.getDispatcher().registerListener(commandHandler);
    }
}
