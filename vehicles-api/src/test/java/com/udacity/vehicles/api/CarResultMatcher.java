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
            // object with "content" & "links"
            JsonNode contentNode = node.get("content");
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
        assertTrue(found, () -> Arrays.toString(stringList.toArray(new String[0])) + " does not match " + content);
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
