package com.udacity.vehicles.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.data.util.Pair;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ResultMatcher class to check car json
 */
class CarResultMatcher implements org.springframework.test.web.servlet.ResultMatcher {

    enum Mode {LIST, OBJECT}

    List<String> stringList;
    Mode mode;
    String selection;
    Logger log;

    public CarResultMatcher(List<String> stringList, Mode mode, String selection, Logger log) {
        this.stringList = stringList;
        this.mode = mode;
        this.selection = selection;
        this.log = log;
    }

    public CarResultMatcher(List<String> stringList, Mode mode, Logger log) {
        this(stringList, mode, null, log);
    }

    public CarResultMatcher(String jsonStr, Mode mode, Logger log) {
        this(Collections.singletonList(jsonStr), mode, null, log);
    }

    public CarResultMatcher(String jsonStr, Mode mode, String selection, Logger log) {
        this(Collections.singletonList(jsonStr), mode, selection, log);
    }

    public static CarResultMatcher of(List<String> stringList, Mode mode, Logger log) {
        return of(stringList, mode, null, log);
    }

    public static CarResultMatcher of(List<String> stringList, Mode mode, String selection, Logger log) {
        return new CarResultMatcher(stringList, mode, selection, log);
    }

    public static CarResultMatcher of(String jsonStr, Mode mode, Logger log) {
        return of(jsonStr, mode, null, log);
    }

    public static CarResultMatcher of(String jsonStr, Mode mode, String selection, Logger log) {
        return new CarResultMatcher(jsonStr, mode, selection, log);
    }

    @Override
    public void match(MvcResult mvcResult) throws Exception {
        String content = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode node = objectMapper.readTree(content);
        assertEquals(JsonNodeType.OBJECT, node.getNodeType());

        boolean found = false;
        if (mode == Mode.LIST) {
            // object with embedded content
            JsonNode contentNode = node.get("_embedded").get("cars");
            assertEquals(JsonNodeType.ARRAY, contentNode.getNodeType());

            for (Iterator<JsonNode> it = contentNode.elements(); it.hasNext(); ) {
                JsonNode element = it.next();
                if (element.getNodeType() == JsonNodeType.OBJECT) {
                    // check if selection criteria is met
                    String elementStr = element.toString();
                    if (StringUtils.isEmpty(selection) || elementStr.matches(selection)) {
                        // search for just the car, ignoring the links
                        found = checkMatchCount(element);
                        if (found) {
                            break;
                        }
                    }
                }
            }
        } else {
            found = checkMatchCount(node);
        }
        assertTrue(found, () ->
            String.format("Matcher %s%n" +
                    "does not match%n" +
                    "Content: %s", Arrays.toString(stringList.toArray(new String[0])), content));
    }

    private boolean checkMatchCount(JsonNode element) {
        String elementStr = element.toString();
        return stringList.stream()
                .map(s -> Pair.of(s, elementStr.contains(s) ? 1 : 0))
                .peek(p -> {
                    if (p.getSecond() == 0 && log != null) {
                        log.info("No match: " + p.getFirst());
                    }
                })
                .mapToInt(Pair::getSecond)
                .sum() == stringList.size();
    }
}

/* Format of list response
{
  "_embedded": {
    "cars": [
      {
        "id": 1,
        "createdAt": "2021-01-10T09:32:01.49814",
        "modifiedAt": "2021-01-10T10:52:09.519065",
        "condition": "NEW",
        "details": {
          "body": "string",
          "model": "string",
          "manufacturer": {
            "code": 100,
            "name": "Audi"
          },
          "numberOfDoors": 0,
          "fuelType": "string",
          "engine": "string",
          "mileage": 0,
          "modelYear": 0,
          "productionYear": 0,
          "externalColor": "string"
        },
        "location": {
          "lat": 0,
          "lon": 0,
          "address": "5335 Hwy 280 South",
          "city": "Hoover",
          "state": "AL",
          "zip": "35242"
        },
        "price": "€ 16350.10",
        "_links": {
          "self": {
            "href": "http://localhost:8080/cars/1"
          },
          "cars": {
            "href": "http://localhost:8080/cars"
          }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "http://localhost:8080/cars"
    }
  }
}
 */