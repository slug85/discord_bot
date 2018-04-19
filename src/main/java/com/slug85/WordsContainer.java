package com.slug85;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by sergey.lugovskoi on 19.04.2018.
 */
@Component
public class WordsContainer implements InitializingBean{

    private static final Logger LOGGER = LoggerFactory.getLogger(WordsContainer.class.getName());

    private ArrayList<String> stopWords;

    public List<String> getStopWords(){
        return stopWords;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.info("LOAD Stop-word_dic.txt");
        try {
            File file  = new ClassPathResource("Stop-word_dic.txt").getFile();
            try {
                Scanner scanner = new Scanner(file);
                while(scanner.hasNextLine()){
                    this.stopWords.add(scanner.nextLine());
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
        catch (IOException ignored) {}
    }
}
