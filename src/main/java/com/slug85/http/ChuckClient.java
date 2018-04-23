package com.slug85.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * Created by sergey.lugovskoi on 20.04.2018.
 */

public class ChuckClient implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChuckClient.class);

    @Autowired
    private ObjectMapper objectMapper;

    private RequestSpecification spec;


    @Override
    public void afterPropertiesSet() throws Exception {
        spec = new RequestSpecBuilder()
                .setBaseUri("http://api.icndb.com/jokes/")
                .setBasePath("random")
                .build();
    }

    public Joke getJoke(){
        Response response = RestAssured.given().spec(spec)
                .get();
        Joke joke;
        try {
            joke = objectMapper.readValue(response.asString(), Joke.class);
        } catch (IOException e) {
            joke = new Joke();
        }
        return joke;
    }
}
