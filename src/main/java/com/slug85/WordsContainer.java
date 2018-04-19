package com.slug85;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by sergey.lugovskoi on 19.04.2018.
 */
public class WordsContainer implements InitializingBean{

    private static final Logger LOGGER = LoggerFactory.getLogger(WordsContainer.class.getName());

    private ArrayList<String> stopWords = new ArrayList<>();

    List<String> getStopWords(){
        if(stopWords.size() == 0){
            loadWords();
        }
        return stopWords;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadWords();
    }

    private void loadWords(){
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
        catch (IOException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
