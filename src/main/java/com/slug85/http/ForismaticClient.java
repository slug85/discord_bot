package com.slug85.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.SSLConfig;
import io.restassured.response.Response;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static io.restassured.config.SSLConfig.sslConfig;

/**
 * Created by sergey.lugovskoi on 20.04.2018.
 */

public class ForismaticClient  implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForismaticClient.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterPropertiesSet() throws Exception {
        RestAssured.baseURI = "http://api.forismatic.com/";
        RestAssured.basePath = "api/1.0/";
        SSLConfig sslConfig = sslConfig().allowAllHostnames().relaxedHTTPSValidation();
        RestAssured.config = RestAssured.config().sslConfig(sslConfig);
        //RestAssured.proxy = host("tmg.soglasie.ru").withPort(8080).withAuth("usrprog","Zappy78rEe$");
        RestAssured.useRelaxedHTTPSValidation();
    }

    public Quote getQuote(){
        Response response = RestAssured.given()
                .param("method", "getQuote")
                .param("format", "json")
                .param("lang", "ru")
                .post();
        Quote quote = null;
        try {
            quote = objectMapper.readValue(response.asString(), Quote.class);
        } catch (IOException e) {
            quote = new Quote();
        }
        return quote;
    }

    private JsonNode parseResponse(Response response){
        JsonNode node = null;
        try {
            response.prettyPrint();
            node = objectMapper.readTree(response.asString());
        } catch (IOException e) {
            LOGGER.error(ExceptionUtils.getStackTrace(e));
        }
        return node;
    }
}
