package personal.fedorbarinov.vkliketracker.parsing;

import java.io.InputStream;
import java.util.Map;

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
     * Parse input source for parameters and their values
     * @param in Input source stream
     * @return Map of the parameters and values
     * @throws ParsingException Exception that is thrown during parsing
     */
    Map <String, String> parse(InputStream in) throws ParsingException;
}
