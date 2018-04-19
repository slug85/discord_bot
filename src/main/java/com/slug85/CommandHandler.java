package com.slug85;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by sergey.lugovskoi on 19.04.2018.
 */
@Component
public class CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);
    private Launcher connector;

    @Resource(name = "badWords")
    private HashMap<String, Integer> stopWordsCount;

    @Resource(name = "stopWords")
    private List<String> stopWords;

    @Autowired
    private void setConnector(Launcher c) {
        connector = c;
    }

    private void sendMessage(IChannel channel, String message) {
        RequestBuffer.request(() -> {
            try {
                channel.sendMessage(message);
            } catch (DiscordException e) {
                System.err.println("Message could not be sent with error: ");
                e.printStackTrace();
            }
        });
    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
        String s = event.getMessage().getContent().toLowerCase();

        String authorName = event.getMessage().getAuthor().getName();

        if(s.contains("рестартую пилот") || s.contains("рестарт") ){

            //random emoji
            Random r = new Random();
            Collection<Emoji> all = EmojiManager.getAll();
            Emoji e = EmojiManager.getAll().stream().skip(r.nextInt(all.size()-1)).findFirst().orElse(EmojiManager.getForAlias("heart"));
            event.getMessage().addReaction(e);

            this.sendMessage(event.getChannel(), "НЕТ!!!!");
        }

        //счетчик ругательств и добавление Х эмоджи
        String[] rushWords = s.split(" ");
        boolean reaction = false;
        for(String word: rushWords){
            if(stopWords.contains(word)){
                if(!reaction){
                    event.getMessage().addReaction(EmojiManager.getForAlias("x"));
                    reaction = true;
                }
                int count = stopWordsCount.get(authorName)==null?0: stopWordsCount.get(authorName);
                count++;
                stopWordsCount.put(authorName, count);
                String msg = event.getAuthor().mention() + " ругался уже " + count + " раз!";
                this.sendMessage(event.getChannel(), msg);
            }
        }




    }

    @EventSubscriber
    public void onUser(UserJoinEvent event) {
        String response = "Pathetic " + event.getUser().mention();
        this.sendMessage(event.getGuild().getDefaultChannel(), response);
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


