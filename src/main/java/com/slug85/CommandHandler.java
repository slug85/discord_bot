package com.slug85;

import com.slug85.command.CommandUtils;
import com.slug85.db.MessageDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sergey.lugovskoi on 19.04.2018.
 */
@Component
@Scope(value = "singleton")
public class CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);
    private Launcher connector;

    @Autowired
    private CommandUtils commandUtils;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private void setConnector(Launcher c) {
        connector = c;
    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
        String s = event.getMessage().getContent().toLowerCase();

        String[] argArray = event.getMessage().getContent().split(" ");

        // Extract the "command" part of the first arg out by just ditching the first character
        String commandStr = argArray[0].substring(1);

        // Load the rest of the args in the array into a List for safer access
        List<String> argsList = new ArrayList<>(Arrays.asList(argArray));
        argsList.remove(0); // Remove the command

        if(commandUtils.getCommandMap().containsKey(commandStr)){
            commandUtils.getCommandMap().get(commandStr).runCommand(event, argsList);
        }else {

            if (s.toLowerCase().contains("рестартую пилот")
                    || s.toLowerCase().contains("рестарт")
                    || s.toLowerCase().contains("пилот рестарт")
                    ) {
                log.info("РЕСТАРТ ПИЛОТА");
                commandUtils.restartPilot(event);
            }

            boolean isRush = commandUtils.checkRushWords(event);
            boolean botRush = commandUtils.checkBotRushWords(event);
            messageDao.saveMessage(event.getMessage(), isRush, botRush);
        }

    }

    @EventSubscriber
    public void onUser(UserJoinEvent event) {
        String response = "Pathetic " + event.getUser().mention();
        commandUtils.sendMessage(event.getGuild().getDefaultChannel(), response);
    }

    @EventSubscriber
    public void onDisconnect(DisconnectedEvent event) throws Exception {
        try {
            log.info("DISCONNECTED " + event.getReason().toString());
            connector.login();

        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
    }




}


