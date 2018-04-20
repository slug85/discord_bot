package com.slug85;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.slug85.http.ForismaticClient;
import com.slug85.http.Quote;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.endpoint.MetricsEndpoint;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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

    @Autowired
    private WordsContainer wordsContainer;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ForismaticClient forismaticClient;

    @Autowired
    private Launcher launcher;

    @Autowired
    private MetricsEndpoint metricsEndpoint;

    @Autowired
    private HealthEndpoint healthEndpoint;


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

    private void sendSelfPortrait(MessageReceivedEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        String avatarUrl = launcher.getClient().getApplicationIconURL();
        builder.withImage(avatarUrl);
        RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));

    }

    private void sendQuote(MessageReceivedEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        Quote quote = forismaticClient.getQuote();
        builder.withDesc(quote.quoteText);
        builder.withFooterText(quote.quoteAuthor);
        RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));

    }

    private void sendMetrics(MessageReceivedEvent event) {
        JsonNode metrics = getMetrics();
        String pretty;
        try {
            pretty = "```" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(metrics) + "```";

            EmbedBuilder builder = new EmbedBuilder();
            builder.withAuthorName(
                    ApplicationContextProvider.getApplicationContext().getApplicationName() +
                    System.lineSeparator() +
                    ApplicationContextProvider.getApplicationContext().getId());
            builder.withAuthorIcon("https://avatars2.githubusercontent.com/u/37113093?s=40&v=4");
            builder.withAuthorUrl("https://github.com/slug85/discord_bot");
            builder.withDescription(pretty);
            RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));

        } catch (JsonProcessingException ignored) {
        }
    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
        String s = event.getMessage().getContent().toLowerCase();

        String authorName = event.getMessage().getAuthor().getName();

        if(s.toLowerCase().contains("рестартую пилот") || s.contains("рестарт") ){
            //random emoji
            Random r = new Random();
            Collection<Emoji> all = EmojiManager.getAll();
            Emoji e = EmojiManager.getAll().stream().skip(r.nextInt(all.size()-1)).findFirst().orElse(EmojiManager.getForAlias("heart"));
            event.getMessage().addReaction(e);

            this.sendMessage(event.getChannel(), event.getAuthor().mention() + " НЕТ!!!!");
        }

        //метрики
        if(s.toLowerCase().contains("!бот как дела")){
            sendMetrics(event);
        }
        //цитата
        if(s.toLowerCase().contains("!бот голос")){
            sendQuote(event);
        }

        //картинка
        if(s.contains("!бот портрет")){
            sendSelfPortrait(event);
        }

        //счетчик ругательств и добавление Х эмоджи
        String[] rushWords = s.split(" ");
        boolean reaction = false;

        //слова из сообщения
        for(String word: rushWords){

            if(wordsContainer.getStopWords().contains(word.toLowerCase())){
                if(!reaction){
                    event.getMessage().addReaction(EmojiManager.getForAlias("x"));
                    reaction = true;
                }
                Integer count = stopWordsCount.get(authorName) == null ? 0 : stopWordsCount.get(authorName);
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


    private JsonNode getMetrics(){
        ObjectNode node = mapper.createObjectNode();
        Map metrics = metricsEndpoint.invoke();
        Health health = healthEndpoint.invoke();
        health.getDetails().forEach((k,v)-> node.put(k, v.toString()));

        //noinspection unchecked
        metrics.forEach((k ,v) -> node.put(k.toString(), v.toString()));
        return node;
    }

}


