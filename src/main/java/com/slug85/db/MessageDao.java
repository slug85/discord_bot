package com.slug85.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.obj.IMessage;

import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class MessageDao{

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired
    private NamedParameterJdbcTemplate template;

    public void saveMessage(IMessage message, boolean isRush, boolean botRush){

        MapSqlParameterSource params = new MapSqlParameterSource();

        params.addValue("channel", message.getChannel().getName(), Types.VARCHAR);
        params.addValue("user", message.getAuthor().getName(), Types.VARCHAR);
        params.addValue("content", message.getContent(), Types.VARCHAR);
        params.addValue("guild", message.getGuild().getName(), Types.VARCHAR);
        params.addValue("is_rush", isRush, Types.BOOLEAN);
        params.addValue("bot_rush", botRush, Types.BOOLEAN);
        params.addValue("date", Date.from(message.getTimestamp()), Types.TIMESTAMP);

        template.update(
                "insert into messages " +
                        "(user_name, channel, content, is_rush, bot_rush, date, guild)" +
                        " values " +
                        "(:user, :channel, :content, :is_rush, :bot_rush ,:date, :guild)", params);

        LOGGER.info("save message " + message.getStringID() + " is_rush="+isRush + " bot_rush="+botRush);
    }

    public Map<String, Integer> getMostRudeUser(String guild){
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("guild", guild, Types.VARCHAR);

        String query = "select user_name, count(*), guild from messages\n" +
                " group by user_name,guild, is_rush " +
                " having guild = :guild " +
                " and is_rush = true " +
                " limit 5";

        return template.query(query, params, (ResultSetExtractor<Map<String, Integer>>) rs -> {
            HashMap<String,Integer> map = new HashMap<>();
            while(rs.next()){
                map.put(rs.getString("user_name"),rs.getInt("count"));
            }
            return map;
        });

    }
}
