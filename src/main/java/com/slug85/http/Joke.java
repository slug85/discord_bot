package com.slug85.http;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by sergey.lugovskoi on 20.04.2018.
 */
public class Joke {

    /*
    {
  "type": "success",
  "value": {
    "id": 384,
    "joke": "Chuck Norris was the orginal sculptor of Mount Rushmore. He completed the entire project using only a bottle opener and a drywall trowel.",
    "categories": [

    ]
  }
}
     */

    public String type;
    public Value value = new Value();

    @JsonIgnore
    public Object categories;

    public static class Value{

        public int id;
        public String joke = " пока не придумал ";

    }

}


