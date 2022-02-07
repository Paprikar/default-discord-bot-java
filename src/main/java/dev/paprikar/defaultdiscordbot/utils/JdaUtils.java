package dev.paprikar.defaultdiscordbot.utils;

import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Consumer;

public class JdaUtils {

    private static final Logger logger = LoggerFactory.getLogger(JdaUtils.class);

    public static class RequestErrorHandler implements Consumer<Throwable> {

        private final String message;

        private final Runnable action;

        private final EnumSet<ErrorResponse> warningResponses;

        RequestErrorHandler(String message, Runnable action, EnumSet<ErrorResponse> warningResponses) {
            this.message = message;
            this.action = action;
            this.warningResponses = warningResponses;
        }

        public static RequestErrorHandlerBuilder createBuilder() {
            return new RequestErrorHandlerBuilder();
        }

        @Override
        public void accept(Throwable throwable) {
            action.run();

            if (throwable instanceof ErrorResponseException) {
                ErrorResponseException ere = (ErrorResponseException) throwable;
                if (warningResponses.contains(ere.getErrorResponse())) {
                    logger.warn(message, throwable);
                    return;
                }
            }

            logger.error(message, throwable);
        }

        public static class RequestErrorHandlerBuilder {

            private final EnumSet<ErrorResponse> warningResponses = EnumSet.noneOf(ErrorResponse.class);

            private String message = "An error occurred while executing the request";

            private Runnable action = () -> {};

            public RequestErrorHandlerBuilder setMessage(String message) {
                this.message = message;
                return this;
            }

            public RequestErrorHandlerBuilder setAction(Runnable action) {
                this.action = action;
                return this;
            }

            public RequestErrorHandlerBuilder warnOn(ErrorResponse response) {
                warningResponses.add(response);
                return this;
            }

            public RequestErrorHandlerBuilder warnOn(ErrorResponse... responses) {
                Collections.addAll(warningResponses, responses);
                return this;
            }

            public RequestErrorHandler build() {
                return new RequestErrorHandler(message, action, warningResponses);
            }
        }
    }
}
