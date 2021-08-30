package dev.paprikar.defaultdiscordbot.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultObjectMapper {

    public static final ObjectMapper INSTANCE = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(DefaultObjectMapper.class);

    public static String serializeAsString(Object o) {
        try {
            return DefaultObjectMapper.INSTANCE.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            logger.error("An error occurred during object serialization", e);
            throw new RuntimeException(e);
        }
    }
}
