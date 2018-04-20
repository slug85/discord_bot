package com.slug85;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by sergey.lugovskoi on 19.04.2018.
 */
public class WordsContainer implements InitializingBean{

    private static final Logger LOGGER = LoggerFactory.getLogger(WordsContainer.class.getName());

    private ArrayList<String> stopWords = new ArrayList<>();

    public List<String> getStopWords(){
        return stopWords;
    }

    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public void afterPropertiesSet() throws Exception {
        loadWords();
    }

    private void loadWords(){
        LOGGER.info("LOAD Stop-word_dic.txt");
        try {

            Resource resource = resourceLoader.getResource("classpath:Stop-word_dic.txt");
            File file = new File("Stop-word_dic.txt");
            InputStream inputStream = resource.getInputStream();
            OutputStream outputStream = new FileOutputStream(file);
            IOUtils.copy(inputStream, outputStream);
            outputStream.close();


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
