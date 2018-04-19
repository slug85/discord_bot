package com.slug85;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by sergey.lugovskoi on 19.04.2018.
 */
@Configuration
public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class.getName());

    @Bean(name = "stopWords")
    public List<String> loadStopWords(){
        List<String> words = new ArrayList<>();
        LOGGER.info("LOAD Stop-word_dic.txt");
        try {
            File file  = new ClassPathResource("Stop-word_dic.txt").getFile();
            try {
                Scanner scanner = new Scanner(file);
                while(scanner.hasNextLine()){
                    words.add(scanner.nextLine());
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
        catch (IOException ignored) {}
        return words;
    }

}
