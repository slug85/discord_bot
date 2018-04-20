package com.slug85.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.slug85.ApplicationContextProvider;
import com.slug85.Launcher;
import com.slug85.WordsContainer;
import com.slug85.http.ForismaticClient;
import com.slug85.http.Quote;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.actuate.endpoint.MetricsEndpoint;
import org.springframework.boot.actuate.health.Health;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
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
 * Created by sergey.lugovskoi on 20.04.2018.
 */
public class CommandUtils implements InitializingBean{

    @Autowired
    private Launcher launcher;

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandUtils.class.getName());

    public final static String BOT_PREFIX = "!";
    private Map<String, Command> commandMap = new HashMap<>();

    public Map<String, Command> getCommandMap() {
        return commandMap;
    }

    @Autowired
    private ForismaticClient forismaticClient;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MetricsEndpoint metricsEndpoint;

    @Autowired
    private HealthEndpoint healthEndpoint;

    @Resource(name = "badWords")
    private HashMap<String, Integer> stopWordsCount;

    @Autowired
    private WordsContainer wordsContainer;

    public void sendMessage(IChannel channel, String message) {
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
        String avatarUrl = "https://cs8.pikabu.ru/post_img/2017/05/25/4/og_og_1495685192266522652.jpg";
        builder.withImage(avatarUrl);
        RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));

    }

    private void showTea(MessageReceivedEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        String teaUrl = "https://media.discordapp.net/attachments/436423792487563296/436823176446476288/rabstol_net_tea_09.jpg";
        builder.withImage(teaUrl);
        RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));

    }

    private void sendQuote(MessageReceivedEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        Quote quote = forismaticClient.getQuote();
        builder.withDesc(quote.quoteText);
        builder.withFooterText(quote.quoteAuthor);
        RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));

    }

    public void restartPilot(MessageReceivedEvent event){
        Random r = new Random();
        Collection<Emoji> all = EmojiManager.getAll();
        Emoji e = EmojiManager.getAll().stream().skip(r.nextInt(all.size()-1)).findFirst().orElse(EmojiManager.getForAlias("heart"));
        event.getMessage().addReaction(e);

        sendMessage(event.getChannel(), event.getAuthor().mention() + " НЕТ!!!!");
    }

    public void checkRushWords(MessageReceivedEvent event){
        //счетчик ругательств и добавление Х эмоджи
        String[] rushWords = event.getMessage().getContent().split(" ");
        boolean reaction = false;

        String authorName = event.getAuthor().getName();
        //слова из сообщения
        for(String word: rushWords){

            if(wordsContainer.getStopWords().contains(word.toLowerCase()) || word.endsWith("ссука")){
                if(!reaction){
                    event.getMessage().addReaction(EmojiManager.getForAlias("x"));
                    reaction = true;
                }
                Integer count = stopWordsCount.get(authorName) == null ? 0 : stopWordsCount.get(authorName);
                count++;
                stopWordsCount.put(authorName, count);
                String msg = event.getAuthor().mention() + " ругался уже " + count + " раз!";
                sendMessage(event.getChannel(), msg);
            }
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

    private void showHelp(MessageReceivedEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.withDesc("доступные комманды");
        commandMap.forEach((k, v) -> {
            builder.appendField("-->", BOT_PREFIX + k, false);
        });
        RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));
    }

    @Override
    public void afterPropertiesSet(){
        commandMap.put("голос", (event, args) -> sendQuote(event));
        commandMap.put("статус", (event, args) -> sendMetrics(event));
        commandMap.put("портрет", (event, args) -> sendSelfPortrait(event));
        commandMap.put("чай", (event, args) -> showTea(event));
        commandMap.put("help", (event, args) -> showHelp(event));
    }

    public void checkBotRushWords(MessageReceivedEvent event) {
        //упоминание бота
        String s = event.getMessage().getContent();
        if(s.toLowerCase().contains(" бот ") || s.toLowerCase().endsWith(" бот") || s.startsWith("бот ")){
            boolean reacted = false;
            for(String word: s.split(" ")){
                LOGGER.info("rush word " + word);
                if(wordsContainer.getStopWords().contains(word.toLowerCase())){
                    if(!reacted){
                        event.getMessage().addReaction
                                (EmojiManager.getForAlias("angry"));
                        event.getMessage().addReaction
                                (EmojiManager.getForAlias("rage"));
                        reacted = true;
                    }
                }
            }
        }
    }
}
