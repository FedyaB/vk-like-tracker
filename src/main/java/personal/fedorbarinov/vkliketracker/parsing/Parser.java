package personal.fedorbarinov.vkliketracker.parsing;

import java.io.InputStream;

/**
 * Abstract parser
 */
public interface Parser {
    /**
     * Exception that is thrown during parsing
     */
    class ParsingException extends Exception {
        ParsingException(String s) {
            super(s);
        }
    }

    /**
     * Result of parsing
     */
    interface ParsingResult {
        /**
         * Get value by key
         * @param key Key
         * @return Value associated with key
         */
        String get(String key);

        /**
         * Put data into result
         * @param key Key
         * @param value Value associated with key
         */
        void put(String key, String value);

        /**
         * Check whether key is present in the result
         * @param key Key
         * @return True if key is present (False otherwise)
         */
        boolean contains(String key);
    }

    /**
     * Parse input source for parameters and their values
     * @param in Input source stream
     * @return Parsing result of parameters and options
     * @throws ParsingException Exception that is thrown during parsing
     */
    ParsingResult parse(InputStream in) throws ParsingException;
}
