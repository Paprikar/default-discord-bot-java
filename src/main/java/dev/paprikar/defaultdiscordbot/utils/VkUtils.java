package dev.paprikar.defaultdiscordbot.utils;

import com.vk.api.sdk.client.ApiRequest;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for VK SDK.
 */
public class VkUtils {

    private static final Logger logger = LoggerFactory.getLogger(VkUtils.class);

    /**
     * Executes the request and returns {@code null} in case of any errors.
     *
     * @param request
     *         request to execute
     * @param <T>
     *         the type of the returned request object
     *
     * @return the object returned by the request, or {@code null} if error occurred during the request
     */
    public static <T> T executeRequest(ApiRequest<T> request) {
        try {
            return request.execute();
        } catch (ClientException | ApiException e) {
            logger.warn("executeRequest(): An error occurred while executing the request", e);
            return null;
        }
    }
}
