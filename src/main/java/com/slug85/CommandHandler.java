package com.slug85;

import com.slug85.command.CommandUtils;
import com.vdurmont.emoji.EmojiManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);
    private Launcher connector;

    @Autowired
    private CommandUtils commandUtils;

    @Autowired
    private WordsContainer wordsContainer;

    @Autowired
    private void setConnector(Launcher c) {
        connector = c;
    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
        String s = event.getMessage().getContent().toLowerCase();

        String[] argArray = event.getMessage().getContent().split(" ");

        // First ensure at least the command and prefix is present, the arg length can be handled by your command func
        if(argArray.length == 0)
            return;

        // Check if the first arg (the command) starts with the prefix defined in the utils class
        if(!argArray[0].startsWith(CommandUtils.BOT_PREFIX))
            return;

        // Extract the "command" part of the first arg out by just ditching the first character
        String commandStr = argArray[0].substring(1);

        // Load the rest of the args in the array into a List for safer access
        List<String> argsList = new ArrayList<>(Arrays.asList(argArray));
        argsList.remove(0); // Remove the command

        if(commandUtils.getCommandMap().containsKey(commandStr))
            commandUtils.getCommandMap().get(commandStr).runCommand(event, argsList);

        if(s.toLowerCase().contains("рестартую пилот")
                || s.toLowerCase().contains("рестарт")
                || s.toLowerCase().contains("пилот рестарт")
                ){
            log.info("РЕСТАРТ ПИЛОТА");
            commandUtils.restartPilot(event);
        }

        //упоминание бота
        if(s.toLowerCase().contains(" бот ") || s.toLowerCase().contains(" бот.") || s.contains("Бот ")){
            boolean reacted = false;
            for(String word: s.split(" ")){
                if(wordsContainer.getStopWords().contains(word.toLowerCase())){
                    if(!reacted){
                        event.getMessage().addReaction(EmojiManager.getForAlias("poo"));
                        event.getMessage().addReaction(EmojiManager.getForAlias("rage"));
                        reacted = true;
                    }
                }
            }
        }

        commandUtils.checkRushWords(event);
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


