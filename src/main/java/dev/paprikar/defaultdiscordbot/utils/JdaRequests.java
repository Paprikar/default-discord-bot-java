package dev.paprikar.defaultdiscordbot.utils;

import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.function.Consumer;

/**
 * JDA request utilities.
 */
public class JdaRequests {

    private static final Logger logger = LoggerFactory.getLogger(JdaRequests.class);

    /**
     * The request error handler.
     */
    public static class RequestErrorHandler implements Consumer<Throwable> {

        private final String message;

        private final Runnable action;

        private final EnumSet<ErrorResponse> warningResponses;

        /**
         * Constructs a request error handler.
         *
         * @param message
         *         the message for error logging
         * @param action
         *         the action on an error
         * @param warningResponses
         *         a set of errors of type {@link ErrorResponse},
         *         which will be processed as warnings
         */
        RequestErrorHandler(String message, Runnable action, EnumSet<ErrorResponse> warningResponses) {
            this.message = message;
            this.action = action;
            this.warningResponses = warningResponses;
        }

        /**
         * Constructs a builder for the request error handler.
         *
         * @return the builder for the request error handler
         */
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

        /**
         * The builder for the request error handler.
         */
        public static class RequestErrorHandlerBuilder {

            private final EnumSet<ErrorResponse> warningResponses = EnumSet.noneOf(ErrorResponse.class);

            private String message = "An error occurred while executing the request";

            private Runnable action = () -> {};

            /**
             * @param message
             *         the message for error logging
             *
             * @return the current {@link RequestErrorHandlerBuilder} instance. Useful for chaining
             */
            public RequestErrorHandlerBuilder setMessage(String message) {
                this.message = message;
                return this;
            }


            /**
             * @param action
             *         the action on an error
             *
             * @return the current {@link RequestErrorHandlerBuilder} instance. Useful for chaining
             */
            public RequestErrorHandlerBuilder setAction(Runnable action) {
                this.action = action;
                return this;
            }

            /**
             * Adds an error response to the warning error set.
             *
             * @param response
             *         error response to add to the warning error set
             *
             * @return the current {@link RequestErrorHandlerBuilder} instance. Useful for chaining
             */
            public RequestErrorHandlerBuilder warnOn(ErrorResponse response) {
                warningResponses.add(response);
                return this;
            }

            /**
             * Adds error responses to the warning error set.
             *
             * @param responses
             *         error responses to add to the warning error set
             *
             * @return the current {@link RequestErrorHandlerBuilder} instance. Useful for chaining
             */
            public RequestErrorHandlerBuilder warnOn(ErrorResponse... responses) {
                Collections.addAll(warningResponses, responses);
                return this;
            }

            /**
             * Builds a request error handler.
             *
             * @return the request error handler
             */
            public RequestErrorHandler build() {
                return new RequestErrorHandler(message, action, warningResponses);
            }
        }
    }
}
