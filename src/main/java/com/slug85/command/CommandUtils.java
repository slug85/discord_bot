package com.slug85.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.slug85.ApplicationContextProvider;
import com.slug85.WordsContainer;
import com.slug85.db.MessageDao;
import com.slug85.http.*;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import ga.dryco.redditjerk.wrappers.Link;
import ga.dryco.redditjerk.wrappers.RedditThread;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sergey.lugovskoi on 20.04.2018.
 */
public class CommandUtils implements InitializingBean{

    @Autowired
    private volatile MessageDao messageDao;

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandUtils.class.getName());

    public final static String BOT_PREFIX = "!";
    private Map<String, Command> commandMap = new HashMap<>();

    public Map<String, Command> getCommandMap() {
        return commandMap;
    }

    @Autowired
    private ForismaticClient forismaticClient;

    @Autowired
    private MyRedditClient reddit;

    @Autowired
    private ChuckClient chuckClient;

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


    @Override
    public void afterPropertiesSet(){
        commandMap.put("голос", (event, args) -> sendQuote(event));
        commandMap.put("статус", (event, args) -> sendMetrics(event));
        commandMap.put("портрет", (event, args) -> sendSelfPortrait(event));
        commandMap.put("чай", (event, args) -> showTea(event));
        commandMap.put("чак", (event, args) -> sendJoke(event));
        commandMap.put("help", (event, args) -> showHelp(event));
        commandMap.put("грубияны", (event, args) -> showRudeUsers(event));
        commandMap.put("memes", (event, args) -> showMeme(event));
    }

    private void showMeme(MessageReceivedEvent event) {
        RedditThread meme = reddit.getRandomMeme();

        if(meme!=null) {

            StringBuilder message = new StringBuilder();
            Link data = meme.getSubmissionPost().getData();

            message.append("\n");
            message.append(data.getTitle());
            message.append("\n");

            if(data.getUrl().endsWith(".gif")
                    || data.getUrl().endsWith(".gifv")
                    || data.getUrl().endsWith(".png")
                    || data.getUrl().endsWith(".jpeg")
                    || data.getUrl().endsWith(".jpg")
                    || data.getUrl().contains("imgur")
                    || data.getUrl().contains("gfycat")
                    ){

                if(data.getUrl().contains("imgur")){
                    data.setUrl(data.getUrl() + ".jpg");
                }

                message.append("\n");
                message.append(data.getUrl());
            }else{
                message.append("\n");
                message.append("https://www.reddit.com/");
                message.append(data.getPermalink());
            }


            sendMessage(event.getChannel(), message.toString());
        }
    }

    private void showRudeUsers(MessageReceivedEvent event) {
        Map<String, Integer> rudeMap = messageDao.getMostRudeUser(event.getGuild().getName());
        EmbedBuilder builder = new EmbedBuilder();
        builder.withDesc("грубияны канала "+ event.getGuild().getName());
        rudeMap.forEach((k, v) -> builder.appendField("автор "+k, "ругался "+v+" раз ", false));
        RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));
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

    private void sendJoke(MessageReceivedEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        Joke joke = chuckClient.getJoke();
        builder.withDesc(joke.value.joke.replaceAll("&quot;", "'"));
        RequestBuffer.request(() -> event.getChannel().sendMessage(builder.build()));

    }

    public void restartPilot(MessageReceivedEvent event){
        Emoji e = EmojiManager.getForAlias("scream_cat");
        event.getMessage().addReaction(e);
    }

    public boolean checkRushWords(MessageReceivedEvent event){
        //счетчик ругательств и добавление Х эмоджи
        String[] rushWords = event.getMessage().getContent().split(" ");
        boolean reaction = false;
        boolean isRush = false;

        String authorName = event.getAuthor().getName();
        //слова из сообщения
        for(String word: rushWords){

            if(wordsContainer.getStopWords().contains(word.toLowerCase()) || word.endsWith("ссука")){
                if(!reaction){
                    event.getMessage().addReaction(EmojiManager.getForAlias("scream_cat"));
                    //запомнить что это ругательство
                    isRush = true;
                    reaction = true;
                }
                Integer count = stopWordsCount.get(authorName) == null ? 0 : stopWordsCount.get(authorName);
                count++;
                stopWordsCount.put(authorName, count);
                //String msg = event.getAuthor().mention() + " ругался уже " + count + " раз!";
                //sendMessage(event.getChannel(), msg);
            }
        }
        return isRush;
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

    public boolean checkBotRushWords(MessageReceivedEvent event) {
        boolean botRush = false;
        //упоминание бота
        String s = event.getMessage().getContent();
        if(s.toLowerCase().contains(" бот ") || s.toLowerCase().endsWith(" бот") || s.startsWith("бот ")){
            boolean reacted = false;
            for(String word: s.split(" ")){
                LOGGER.info("rush word " + word);
                if(wordsContainer.getStopWords().contains(word.toLowerCase())){
                    if(!reacted){
                        sendMessage(event.getChannel(), event.getMessage().getAuthor().mention() + " я тебя запомнил");
                        reacted = true;
                        botRush = true;
                    }
                }
            }
        }
        return botRush;
    }
}
