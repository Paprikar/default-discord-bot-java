package dev.paprikar.defaultdiscordbot.utils;

import com.vk.api.sdk.client.ApiRequest;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VkUtils {

    private static final Logger logger = LoggerFactory.getLogger(VkUtils.class);

    public static <T> T executeRequest(ApiRequest<T> request) {
        try {
            return request.execute();
        } catch (ClientException | ApiException e) {
            logger.warn("executeRequest(): An error occurred while executing the request", e);
            return null;
        }
    }
}
