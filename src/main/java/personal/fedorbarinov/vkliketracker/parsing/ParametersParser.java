package personal.fedorbarinov.vkliketracker.parsing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

/**
 * Abstract config parameters parser
 */
public abstract class ParametersParser implements Parser {
    private static final String COMMENT_PREFIX = "#"; //Comments in config start with this
    private static final String SEPARATOR_PARAMETER = "="; //A parameter and its value are separated with this
    private static final String EXCEPTION_BAD_FORMAT = "Bad parameters file format (correct: Parameter=Value)";
    private static final String EXCEPTION_NOT_ENOUGH_PARAMETERS = "Not enough parameters";
    private static final String EXCEPTION_PREFIX = "While parsing auth parameters:"; //Prefix for an exception message

    protected Map<String, String> parameters; //Parsed parameters

    protected ParametersParser() {
        parameters = new TreeMap<>();
    }

    @Override
    public Map<String, String> parse(InputStream in) throws ParsingException{
        parameters.clear();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            //Parse config line by line
            while ((line = reader.readLine()) != null)
                parseLine(line);
            //Check whether all the parameters are filled
            if (!isMapFilled())
                throw new ParsingException(buildErrorMessage(EXCEPTION_NOT_ENOUGH_PARAMETERS));
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
        if (parts.length != 2) //Only one parameter-value pair per line is allowed
            throw new ParsingException(buildErrorMessage(EXCEPTION_BAD_FORMAT));
        if (isValidParameter(parts[0])) //If the parameter is present in the config list then add it
            parameters.put(parts[0],parts[1]);
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
     * Check whether the parameter is present in a parameters list of the config
     * @param parameter A parameter name
     * @return True if the parameter is present (False otherwise)
     */
    protected abstract boolean isValidParameter(String parameter);

    /**
     * Check whether all of the config parameters are present in the map
     * @return True if all of the parameters are present (False otherwise)
     */
    protected abstract boolean isMapFilled();
}
