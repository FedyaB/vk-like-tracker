package personal.fedorbarinov.vkliketracker.parsing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Abstract config parameters parser
 */
public abstract class ConfigParser implements Parser {
    private static final String COMMENT_PREFIX = "#"; //Comments in config start with this
    private static final String SEPARATOR_PARAMETER = "="; //A parameter and its value are separated with this
    private static final String OPTION_PREFIX = "-"; //Prefix of an option in config
    private static final String EXCEPTION_BAD_FORMAT = "Bad parameters file format (correct: Parameter=Value)";
    private static final String EXCEPTION_NOT_ENOUGH_PARAMETERS = "Not enough parameters";
    private static final String EXCEPTION_PREFIX = "[Parsing]:"; //Prefix for an exception message

    /**
     * Parsing result returned from config parsers
     */
    private class ConfigParsingResult implements ParsingResult {
        Map<String, String> parameters;
        Set<String> options;

        ConfigParsingResult() {
            parameters = new TreeMap<>();
            options = new TreeSet<>();
        }

        @Override
        public String get(String key) {
            return parameters.getOrDefault(key, null);
        }

        @Override
        public void put(String key, String value) {
            if (value == null)
                options.add(key);
            else
                parameters.put(key, value);
        }

        @Override
        public boolean contains(String key) {
            return parameters.containsKey(key) || options.contains(key);
        }
    }

    ParsingResult parameters; //Parsed parameters

    ConfigParser() {
        parameters = new ConfigParsingResult();
    }

    @Override
    public ParsingResult parse(InputStream in) throws ParsingException{
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            //Parse config line by line
            while ((line = reader.readLine()) != null)
                parseLine(line);
            //Check whether all the parameters are filled
            if (!checkedParametersAppend())
                throw new ParsingException(EXCEPTION_NOT_ENOUGH_PARAMETERS);
            return parameters;
        } catch (IOException e) {
            throw new ParsingException(buildErrorMessage(e.getLocalizedMessage()));
        }
    }

    /**
     * Parse a line of the auth config
     * @param line Line of the file
     * @throws ParsingException Exception that is thrown during parsing
     */
    private void parseLine(String line) throws ParsingException {
        if (line.startsWith(COMMENT_PREFIX)) //Ignore comments
            return;
        String parts[] = line.split(SEPARATOR_PARAMETER);
        if (parts.length == 2) { //PARAMETER=VALUE
            if (isValidParameter(parts[0])) //If the parameter is present in the config list then add it
                parameters.put(parts[0], parts[1]);
        } else if (parts.length == 1 && parts[0].startsWith(OPTION_PREFIX)) { //-OPTION
            String option = parts[0].substring(OPTION_PREFIX.length());
            if (isValidOption(option))
                parameters.put(option, null); //Null in value differs options from parameters
        } else
            throw new ParsingException(buildErrorMessage(EXCEPTION_BAD_FORMAT));
    }

    /**
     * Build error message for an exception
     * @param message Message body
     * @return Built error message
     */
    private static String buildErrorMessage(String message) {
        return EXCEPTION_PREFIX + ' ' + message;
    }

    /**
     * Check whether the parameter is present in parameters list of the config
     * @param parameter A parameter name
     * @return True if the parameter is present (False otherwise)
     */
    protected abstract boolean isValidParameter(String parameter);

    /**
     * Check whether the option is present in options list of the config
     * @param option An option name
     * @return True if the option is present (False otherwise)
     */
    protected abstract boolean isValidOption(String option);

    /**
     * Fill the map with default values
     * @return True if the parameters are filled (False otherwise)
     */
    protected abstract boolean checkedParametersAppend();
}
