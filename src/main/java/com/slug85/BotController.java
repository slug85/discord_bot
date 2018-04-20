package com.slug85;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by sergey.lugovskoi on 19.04.2018.
 */
@RestController
public class BotController {


    @RequestMapping("/")
    public String index() {
        return "Bot is probably alive.";
    }


}
