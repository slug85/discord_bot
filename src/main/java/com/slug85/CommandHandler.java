package com.slug85;

import org.springframework.stereotype.Component;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

/**
 * Created by sergey.lugovskoi on 19.04.2018.
 */
@Component
public class CommandHandler {

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

        if(s.contains("рестартую пилот") || s.contains("рестарт") ){
            this.sendMessage(event.getChannel(), "НЕТ!!!!");
        }

    }

    @EventSubscriber
    public void onUser(UserJoinEvent event) {
        String response = "Pathetic " + event.getUser().mention();
        this.sendMessage(event.getGuild().getDefaultChannel(), response);
    }

}
