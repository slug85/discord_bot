package com.slug85;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by sergey.lugovskoi on 19.04.2018.
 */
@RestController
public class BotController {

    //@Value("${discord.CLIENT_ID}")
    private String clientId;

    //@Value("${discord.CLIENT_SECRET}")
    private String clientSecret;

    @RequestMapping("/")
    public String index() {
        String response = "Bot is probably alive.";
        return response;
    }

}
