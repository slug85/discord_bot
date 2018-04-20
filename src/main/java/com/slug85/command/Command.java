package com.slug85.command;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.List;

/**
 * Created by sergey.lugovskoi on 20.04.2018.
 */
public interface Command {

    void runCommand(MessageReceivedEvent event, List<String> args);

}
